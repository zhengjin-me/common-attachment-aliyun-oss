package me.zhengjin.common.attachment.adapter

import me.zhengjin.common.attachment.autoconfig.AttachmentAliyunOSStorageProperties
import me.zhengjin.common.attachment.controller.vo.AttachmentVO
import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.attachment.po.AttachmentModelHelper
import me.zhengjin.common.attachment.repository.AttachmentRepository
import cn.hutool.core.io.file.FileNameUtil
import cn.hutool.core.util.RandomUtil
import com.aliyun.oss.HttpMethod
import com.aliyun.oss.OSS
import com.aliyun.oss.model.ObjectMetadata
import java.io.InputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

open class AliyunOSSStorageAdapter(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentAliyunOSStorageProperties: AttachmentAliyunOSStorageProperties,
    private val aliyunOSSClient: OSS
) : AttachmentStorageAdapter(attachmentRepository) {

    override fun share(attachmentId: String): String {
        return aliyunOSSClient.generatePresignedUrl(
            attachmentAliyunOSStorageProperties.bucket,
            getAttachment(attachmentId).filePath,
            Date.from(Instant.now().plus(2, ChronoUnit.HOURS)),
            HttpMethod.GET
        ).toString()
    }

    /**
     * 获取文件流
     */
    override fun getAttachmentFileStream(attachment: Attachment): InputStream =
        aliyunOSSClient.getObject(
            attachmentAliyunOSStorageProperties.bucket,
            attachment.filePath!!
        ).objectContent

    /**
     * 附件存储(最终方法)
     * @param file              文件流
     * @param module            业务模块
     * @param businessTypeCode  业务类型代码
     * @param businessTypeName  业务类型名称
     * @param pkId              业务键
     * @param originalFileName  原始文件名称
     * @param fileContentType   文件媒体类型
     * @param fileSize          文件大小(字节)
     * @param readOnly          附件是否只读 仅能预览 不能下载
     */
    override fun saveFiles(
        file: InputStream,
        module: String,
        businessTypeCode: String,
        businessTypeName: String,
        pkId: String?,
        originalFileName: String,
        fileContentType: String,
        fileSize: Long,
        readOnly: Boolean
    ): AttachmentVO {
        AttachmentModelHelper.checkRegister(module, businessTypeCode)
        FileNameUtil.extName(originalFileName)
        val suffix = RandomUtil.randomString("abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ", 5)
        val mainName = FileNameUtil.mainName(originalFileName)
        val extName = FileNameUtil.extName(originalFileName)
        val dateDir = dateRuleDir()
        val fileName = "$mainName-$suffix.$extName"
        // 统一使用/作为目录分隔符
        val storagePath = "$module/$dateDir/$fileName"

        aliyunOSSClient.putObject(
            attachmentAliyunOSStorageProperties.bucket,
            storagePath,
            file,
            ObjectMetadata().also {
                it.contentType = fileContentType
            }
        )

        var attachment = Attachment()
        if (readOnly) attachment.readOnly = true
        attachment.module = module
        attachment.businessTypeCode = if (readOnly) "${businessTypeCode}_ReadOnly" else businessTypeCode
        attachment.businessTypeName = if (readOnly) "$businessTypeName(预览)" else businessTypeName
        attachment.pkId = if (pkId.isNullOrBlank()) null else pkId.toString()
        attachment.fileOriginName = fileName
        attachment.fileType = fileContentType
        attachment.filePath = storagePath
        attachment.fileSize = fileSize.toString()
        attachment = attachmentRepository.save(attachment)
        return if (!attachment.id.isNullOrBlank()) {
            AttachmentVO.transform(attachment)
        } else {
            throw RuntimeException("file save failed!")
        }
    }
}
