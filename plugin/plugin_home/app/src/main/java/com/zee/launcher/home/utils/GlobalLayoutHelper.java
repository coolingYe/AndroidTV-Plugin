package com.zee.launcher.home.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.CategoryBarLayout;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.layout.PageHeaderLayout;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.layout.SwiperLayout;


import java.util.ArrayList;
import java.util.List;


public class GlobalLayoutHelper {
    public static GlobalLayout analysisGlobalLayoutJson() {
        GlobalLayout globalLayout =  JSON.parseObject(GlobalJsonString, GlobalLayout.class);
        Log.i("test", "----json=" + globalLayout.layout.basic.code);
        Log.i("test", "----json=" + globalLayout.layout.basic.content.size());

        JSONArray jSONArray = globalLayout.layout.basic.content;
        if(jSONArray != null){
            for(int i=0; i<jSONArray.size(); i++){
                JSONObject jsonObject = (JSONObject)jSONArray.get(i);
                String code = jsonObject.getString("code");
                if("page-header".equals(code)){
                    PageHeaderLayout pageHeaderLayout = JSON.toJavaObject(jsonObject, PageHeaderLayout.class);
                    globalLayout.layout.basic.pageHeaderLayout = pageHeaderLayout;
                    Log.i("test", "-ShowLogo---json=" + pageHeaderLayout.config.showLogo);
                }else if("category-bar".equals(code)){
                    CategoryBarLayout categoryBarLayout = JSON.toJavaObject(jsonObject, CategoryBarLayout.class);
                    globalLayout.layout.basic.categoryBarLayout = categoryBarLayout;
                    Log.i("test", "-Direction---json=" + categoryBarLayout.config.direction);
                }
            }
        }

        for(int i=0; i<globalLayout.layout.pages.size(); i++){
            PageLayoutDTO pagesDTO = globalLayout.layout.pages.get(i);
            Log.i("test", "----xxxxxxxxx------pagesDTO-->>>>" + pagesDTO.pageType);
            if(pagesDTO.content != null && pagesDTO.content.size() >= 1){
                JSONArray jSONArrayPageContent = pagesDTO.content.get(0).content;
                if("subject".equals(pagesDTO.pageType)){
                    pagesDTO.subjectJSONArray = jSONArrayPageContent;
                }
                handJsonArrayPageContent(pagesDTO, jSONArrayPageContent);
            }
        }

        return globalLayout;
    }

    public static GlobalLayout analysisGlobalLayout(JSONObject globalLayoutJsonObject) {
        GlobalLayout globalLayout =  JSON.toJavaObject(globalLayoutJsonObject, GlobalLayout.class);
        Log.i("test", "----json=" + globalLayout.layout.basic.code);
        Log.i("test", "----json=" + globalLayout.layout.basic.content.size());

        JSONArray jSONArray = globalLayout.layout.basic.content;
        if(jSONArray != null){
            for(int i=0; i<jSONArray.size(); i++){
                JSONObject jsonObject = (JSONObject)jSONArray.get(i);
                String code = jsonObject.getString("code");
                if("page-header".equals(code)){
                    PageHeaderLayout pageHeaderLayout = JSON.toJavaObject(jsonObject, PageHeaderLayout.class);
                    globalLayout.layout.basic.pageHeaderLayout = pageHeaderLayout;
                    Log.i("test", "-ShowLogo---json=" + pageHeaderLayout.config.showLogo);
                }else if("app-category-bar".equals(code)){
                    CategoryBarLayout categoryBarLayout = JSON.toJavaObject(jsonObject, CategoryBarLayout.class);
                    globalLayout.layout.basic.categoryBarLayout = categoryBarLayout;
                    Log.i("test", "-Direction---json=" + categoryBarLayout.config.direction);
                }
            }
        }

        for(int i=0; i<globalLayout.layout.pages.size(); i++){
            PageLayoutDTO pagesDTO = globalLayout.layout.pages.get(i);
            Log.i("test", "----xxxxxxxxx------pagesDTO-->>>>" + pagesDTO.pageType);
            if(pagesDTO.content != null && pagesDTO.content.size() >= 1){
                JSONArray jSONArrayPageContent = pagesDTO.content.get(0).content;
                Log.i("test", "-1111111111--$$$$$$$$$$$$$$$$$$$-------------" + jSONArrayPageContent);
                if("subject".equals(pagesDTO.pageType)){

                    pagesDTO.subjectJSONArray = jSONArrayPageContent;
                }
                handJsonArrayPageContent(pagesDTO, jSONArrayPageContent);
            }
        }

        return globalLayout;
    }

    private static void handJsonArrayPageContent(PageLayoutDTO pagesDTO, JSONArray jSONArrayPageContent){
        if(jSONArrayPageContent != null){
            for(int j=0; j<jSONArrayPageContent.size(); j++){
                JSONObject jsonObject = (JSONObject)jSONArrayPageContent.get(j);
                String code = jsonObject.getString("code");
                Log.i("test", "--************************code->>>>" + code);
                if("swiper".equals(code)){
                    pagesDTO.swiperLayout = JSON.toJavaObject(jsonObject, SwiperLayout.class);
                }else if("app-card-list".equals(code)){
                    AppCardListLayout appCardListLayout = JSON.toJavaObject(jsonObject, AppCardListLayout.class);
                    pagesDTO.appCardListLayoutList.add(appCardListLayout);
                }
            }
        }
    }

    public static void handleGlobalLayoutPages(GlobalLayout globalLayout){
        List<PageLayoutDTO> pagesDTOList = new ArrayList<>();
        for(int i=0; i<globalLayout.layout.pages.size(); i++){
            Log.i("test", "----handleGlobalLayoutPages--------pages->>>>" + i);
            PageLayoutDTO pagesDTO = globalLayout.layout.pages.get(i);
            if("home".equals(pagesDTO.pageType)){
                pagesDTO.pageName = "首页";
                pagesDTO.pageCode = "home";
                pagesDTOList.add(0, pagesDTO);
            }else if("subject".equals(pagesDTO.pageType)){
                if(globalLayout.layout.basic.categoryBarLayout != null){
                    Log.i("test", "----1111111111111111---------------");
                    for (CategoryBarLayout.ContentDTO contentDTO : globalLayout.layout.basic.categoryBarLayout.content) {
                        Log.i("test", "----2222222222222222--------------");
                        PageLayoutDTO pagesDTOTmp = new PageLayoutDTO();
                        Log.i("test", "---$$$$$$$$$$$$$$$$$$$-------------" + pagesDTO.subjectJSONArray.toJSONString());
                        handJsonArrayPageContent(pagesDTOTmp, pagesDTO.subjectJSONArray);
                        if(pagesDTOTmp.swiperLayout != null){
                            Log.i("test", "-----subject-----swiperLayout-->>>>");
                            pagesDTOTmp.swiperLayout.config.appSkus.addAll(contentDTO.appSkus);
                        }
                        if(pagesDTOTmp.appCardListLayoutList != null && pagesDTOTmp.appCardListLayoutList.size() > 0){
                            Log.i("test", "-----subject-----appCardListLayoutList size-->>>>" + pagesDTOTmp.appCardListLayoutList.size());
                            pagesDTOTmp.appCardListLayoutList.get(0).config.appSkus.addAll(contentDTO.appSkus);
                        }
                        pagesDTOTmp.pageType = "subject";
                        pagesDTOTmp.pageName = contentDTO.name;
                        pagesDTOTmp.pageCode = contentDTO.code;
                        pagesDTOList.add(pagesDTOTmp);
                    }
                }
            }
        }
        globalLayout.layout.pages = pagesDTOList;
    }

    public static String GlobalJsonString = "{\n" +
            "    \"global\": {\n" +
            "        \"showFloatTool\": true\n" +
            "    },\n" +
            "    \"layout\": {\n" +
            "        \"basic\": {\n" +
            "            \"name\": \"容器组件\",\n" +
            "            \"code\": \"container\",\n" +
            "            \"config\": {\n" +
            "                \"direction\": \"vertical\"\n" +
            "            },\n" +
            "            \"content\": [\n" +
            "                {\n" +
            "                    \"name\": \"页头组件\",\n" +
            "                    \"code\": \"page-header\",\n" +
            "                    \"config\": {\n" +
            "                        \"showLogo\": true,\n" +
            "                        \"showLogoText\": true\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"分类条组件\",\n" +
            "                    \"code\": \"category-bar\",\n" +
            "                    \"config\": {\n" +
            "                        \"showHome\": true,\n" +
            "                        \"direction\": \"horizontal\",\n" +
            "                        \"linkage\": true\n" +
            "                    },\n" +
            "                    \"content\": [\n" +
            "                        {\n" +
            "                            \"name\": \"健身\",\n" +
            "                            \"code\": \"jianshen\",\n" +
            "                            \"appSkus\": [\n" +
            "                                \"124871767717203977\",\n" +
            "                                \"72741189333311497\",\n" +
            "                                \"72741461254234121\",\n" +
            "                                \"72741650824192002\",\n" +
            "                                \"72741103010340868\"\n" +
            "                            ]\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"name\": \"童趣\",\n" +
            "                            \"code\": \"tongqu\",\n" +
            "                            \"appSkus\": [\n" +
            "                                \"72741549020045313\",\n" +
            "                                \"72740996202389507\",\n" +
            "                                \"72741103010340868\",\n" +
            "                                \"72740787800006659\",\n" +
            "                                \"72740553665568770\"\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"视图插槽组件\",\n" +
            "                    \"code\": \"view-slot\",\n" +
            "                    \"config\": {}\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageType\": \"home\",\n" +
            "                \"config\": {\n" +
            "                    \"direction\": \"absolute\",\n" +
            "                    \"exclude\": []\n" +
            "                },\n" +
            "                \"content\": [\n" +
            "                    {\n" +
            "                        \"name\": \"容器组件\",\n" +
            "                        \"code\": \"container\",\n" +
            "                        \"config\": {\n" +
            "                            \"direction\": \"vertical\"\n" +
            "                        },\n" +
            "                        \"content\": [\n" +
            "                            {\n" +
            "                                \"name\": \"轮播图组件\",\n" +
            "                                \"code\": \"swiper\",\n" +
            "                                \"config\": {\n" +
            "                                    \"displayMode\": \"right-side\",\n" +
            "                                    \"appSkus\": [\n" +
            "                                        \"124871767717203977\",\n" +
            "                                        \"72741748194959363\",\n" +
            "                                        \"72741189333311497\",\n" +
            "                                        \"72740996202389507\",\n" +
            "                                        \"72740787800006659\"\n" +
            "                                    ],\n" +
            "                                    \"viewSize\": 3,\n" +
            "                                    \"maxCount\": 5,\n" +
            "                                    \"interval\": 3000,\n" +
            "                                    \"effect\": \"slide\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"课件卡片列表\",\n" +
            "                                \"code\": \"app-card-list\",\n" +
            "                                \"config\": {\n" +
            "                                    \"appSkus\": [\n" +
            "                                        \"72740553665568770\",\n" +
            "                                        \"72741549020045313\",\n" +
            "                                        \"72741461254234121\",\n" +
            "                                        \"72741650824192002\"\n" +
            "                                    ],\n" +
            "                                    \"title\": \"畅玩互动\",\n" +
            "                                    \"pageSize\": 5,\n" +
            "                                    \"direction\": \"horizontal\",\n" +
            "                                    \"rowCount\": 5,\n" +
            "                                    \"layout\": \"horizontal\",\n" +
            "                                    \"previewDisplay\": \"h\",\n" +
            "                                    \"showAppName\": true,\n" +
            "                                    \"showAppDesc\": true\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"课件卡片列表\",\n" +
            "                                \"code\": \"app-card-list\",\n" +
            "                                \"config\": {\n" +
            "                                    \"appSkus\": [\n" +
            "                                        \"124871767717203977\",\n" +
            "                                        \"72741189333311497\",\n" +
            "                                        \"72740996202389507\",\n" +
            "                                        \"72741748194959363\",\n" +
            "                                        \"72741103010340868\",\n" +
            "                                        \"72740553665568770\",\n" +
            "                                        \"72741549020045313\",\n" +
            "                                        \"72741461254234121\",\n" +
            "                                        \"72740787800006659\"\n" +
            "                                    ],\n" +
            "                                    \"title\": \"儿童乐园\",\n" +
            "                                    \"pageSize\": 5,\n" +
            "                                    \"direction\": \"horizontal\",\n" +
            "                                    \"rowCount\": 5,\n" +
            "                                    \"layout\": \"horizontal\",\n" +
            "                                    \"previewDisplay\": \"v\",\n" +
            "                                    \"showAppName\": true,\n" +
            "                                    \"showAppDesc\": false\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"pageType\": \"subject\",\n" +
            "                \"config\": {\n" +
            "                    \"direction\": \"absolute\",\n" +
            "                    \"exclude\": []\n" +
            "                },\n" +
            "                \"content\": [\n" +
            "                        {\n" +
            "                            \"name\": \"容器组件\",\n" +
            "                            \"code\": \"container\",\n" +
            "                            \"config\": {\n" +
            "                                \"direction\": \"vertical\"\n" +
            "                            },\n" +
            "                            \"content\": [\n" +
            "                                {\n" +
            "                                    \"name\": \"轮播图组件\",\n" +
            "                                    \"code\": \"swiper\",\n" +
            "                                    \"config\": {\n" +
            "                                        \"displayMode\": \"page\",\n" +
            "                                        \"appSkus\": [],\n" +
            "                                        \"viewSize\": 1,\n" +
            "                                        \"maxCount\": 5,\n" +
            "                                        \"interval\": 3000,\n" +
            "                                        \"effect\": \"slide\"\n" +
            "                                    }\n" +
            "                                },\n" +
            "                                {\n" +
            "                                    \"name\": \"课件卡片列表\",\n" +
            "                                    \"code\": \"app-card-list\",\n" +
            "                                    \"config\": {\n" +
            "                                        \"appSkus\": [],\n" +
            "                                        \"title\": \"畅玩互动\",\n" +
            "                                        \"pageSize\": 5,\n" +
            "                                        \"direction\": \"horizontal\",\n" +
            "                                        \"rowCount\": 5,\n" +
            "                                        \"layout\": \"horizontal\",\n" +
            "                                        \"previewDisplay\": \"h\",\n" +
            "                                        \"showAppName\": true,\n" +
            "                                        \"showAppDesc\": false\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}\n";
}
