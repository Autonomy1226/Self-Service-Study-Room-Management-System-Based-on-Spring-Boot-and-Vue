package com.studyroom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {
    
    private Alipay alipay;
    private Wechat wechat;
    
    // Getters and Setters
    public Alipay getAlipay() { return alipay; }
    public void setAlipay(Alipay alipay) { this.alipay = alipay; }
    
    public Wechat getWechat() { return wechat; }
    public void setWechat(Wechat wechat) { this.wechat = wechat; }
    
    public static class Alipay {
        private String appId;
        private String privateKey;
        private String publicKey;
        private String gateway = "https://openapi.alipay.com/gateway.do";
        private String notifyUrl;
        private String returnUrl;
        
        // Getters and Setters
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        
        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
        
        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
        
        public String getGateway() { return gateway; }
        public void setGateway(String gateway) { this.gateway = gateway; }
        
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    }
    
    public static class Wechat {
        private String appId;
        private String mchId;
        private String apiKey;
        private String certPath;
        private String notifyUrl;
        private String returnUrl;
        
        // Getters and Setters
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        
        public String getMchId() { return mchId; }
        public void setMchId(String mchId) { this.mchId = mchId; }
        
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getCertPath() { return certPath; }
        public void setCertPath(String certPath) { this.certPath = certPath; }
        
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    }
}
