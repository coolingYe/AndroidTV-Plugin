package com.zwn.launcher.data.protocol.response;

public class ThemeInfoResp {
    /**
     * 配色主题详情信息
     */
    public ColorTemplateJson colorTemplateJson;

    /**
     * 服务包ID
     */
    public int packId;

    /**
     * 服务包名称
     */
    public String packName;

    /**
     * 服务包类型,0-资源包,1-基础包,2-高级包,3-尊享包
     */
    public int packType;

    public static class ColorTemplateJson {
        public String packageSoftwareCode;
    }
}
