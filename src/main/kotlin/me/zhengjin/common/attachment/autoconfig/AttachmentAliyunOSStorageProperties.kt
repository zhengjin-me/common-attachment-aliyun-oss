package me.zhengjin.common.attachment.autoconfig

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "customize.common.storage.aliyun-oss")
class AttachmentAliyunOSStorageProperties {

    var bucket: String? = null
    var endpoint: String? = null
    var accessKey: String? = null
    var secretKey: String? = null

    fun checkConfig() {
        require(!bucket.isNullOrBlank()) { "请配置aliyun-oss bucket" }
        require(!endpoint.isNullOrBlank()) { "请配置aliyun-oss endpoint" }
        require(!accessKey.isNullOrBlank()) { "请配置aliyun-oss accessKey" }
        require(!secretKey.isNullOrBlank()) { "请配置aliyun-oss secretKey" }
    }
}
