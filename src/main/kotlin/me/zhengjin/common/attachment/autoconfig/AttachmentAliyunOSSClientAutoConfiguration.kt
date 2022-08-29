package me.zhengjin.common.attachment.autoconfig

import com.aliyun.oss.ClientBuilderConfiguration
import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureBefore(AttachmentAliyunOSSStorageAutoConfiguration::class)
@EnableConfigurationProperties(AttachmentAliyunOSStorageProperties::class)
@ConditionalOnProperty(prefix = "customize.common.storage", name = ["type"], havingValue = "aliyun-oss")
class AttachmentAliyunOSSClientAutoConfiguration(
    private val attachmentAliyunOSStorageProperties: AttachmentAliyunOSStorageProperties
) {

    private val logger = LoggerFactory.getLogger(AttachmentAliyunOSSClientAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun aliyunOSSClient(): OSS {
        attachmentAliyunOSStorageProperties.checkConfig()
        return OSSClientBuilder().build(
            attachmentAliyunOSStorageProperties.endpoint!!,
            attachmentAliyunOSStorageProperties.accessKey!!,
            attachmentAliyunOSStorageProperties.secretKey!!,
            ClientBuilderConfiguration().also { it.isSupportCname = true }
        )
    }
}
