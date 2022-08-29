package me.zhengjin.common.attachment.autoconfig

import com.aliyun.oss.OSS
import me.zhengjin.common.attachment.adapter.AliyunOSSStorageAdapter
import me.zhengjin.common.attachment.adapter.AttachmentStorage
import me.zhengjin.common.attachment.repository.AttachmentRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AttachmentAliyunOSStorageProperties::class)
@ConditionalOnProperty(prefix = "customize.common.storage", name = ["type"], havingValue = "aliyun-oss")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class AttachmentAliyunOSSStorageAutoConfiguration(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentAliyunOSStorageProperties: AttachmentAliyunOSStorageProperties,
) {
    private val logger = LoggerFactory.getLogger(AttachmentAliyunOSSStorageAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun attachmentStorage(aliyunOSSClient: OSS?): AttachmentStorage {
        attachmentAliyunOSStorageProperties.checkConfig()
        logger.info("attachment storage type: [aliyun-oss]")
        return AliyunOSSStorageAdapter(
            attachmentRepository,
            attachmentAliyunOSStorageProperties,
            aliyunOSSClient!!
        )
    }
}
