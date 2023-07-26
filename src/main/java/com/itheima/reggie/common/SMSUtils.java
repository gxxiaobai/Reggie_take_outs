package com.itheima.reggie.common;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * aliyun的短信验证码工具类
 */
@Slf4j
public class SMSUtils {
    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }

    public static SendSmsResponse sendSms(String phone,String code) throws Exception {
        // 请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
        // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例使用环境变量获取 AccessKey 的方式进行调用，仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        Client client = SMSUtils.createClient("LTAI5t5vCEpWJfLBQ338uMJH", "cuHdQ4NSy2hDksNYFEnRVt43pqIHr8");
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("瑞吉外卖")
                .setTemplateCode("SMS_461976227")
                .setPhoneNumbers(phone)
                .setTemplateParam("{\"code\":\""+ code+"\"}");
        RuntimeOptions runtime = new RuntimeOptions();
        // 复制代码运行请自行打印 API 的返回值
        log.info("验证码已发送:{}",code);
       return client.sendSmsWithOptions(sendSmsRequest, runtime);
    }
}
