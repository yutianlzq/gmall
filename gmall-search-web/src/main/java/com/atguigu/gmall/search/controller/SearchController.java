package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import com.atguigu.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.util.StringUtil;

import java.util.*;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-15 20:09
 * description:
 **/
@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){//三级分类Id，关键字

        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfoList);

        //抽取检索结果所包含的平台属性集合
        Set<String> valueIdSet =new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

//         //对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
//        String[] delValueIds = pmsSearchParam.getValueId();
//        if (delValueIds != null) {
//            // 面包屑
//            // pmsSearchParam
//            // delValueIds
//            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
//            for (String delValueId : delValueIds) {
//                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
//                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
//                // 生成面包屑的参数
//                pmsSearchCrumb.setValueId(delValueId);
//                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam, delValueId));
//                while (iterator.hasNext()) {
//                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
//                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
//                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
//                        String valueId = pmsBaseAttrValue.getId();
//                        if (delValueId.equals(valueId)) {
//                            // 查找面包屑的属性值名称
//                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
//                            //删除该属性值所在的属性组
//                            iterator.remove();
//                        }
//                    }
//                }
//                pmsSearchCrumbs.add(pmsSearchCrumb);
//            }
//            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
//        }
        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos=attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfos);

        //对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        String[] delValueId = pmsSearchParam.getValueId();
        if (delValueId != null){
            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
            while (iterator.hasNext()){
                PmsBaseAttrInfo pmsBaseAttrInfo1 = iterator.next();
                List<PmsBaseAttrValue> valueList = pmsBaseAttrInfo1.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : valueList) {
                    String id = pmsBaseAttrValue.getId();
                    for (String s : delValueId) {
                        if (s.equals(id)){
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
            }
        }

        //拿到url
        String urlParam =getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);
        }

        //面包屑
        //pmsSearchParam
        //delValueId
        List<PmsSearchCrumb> pmsSearchCrumbs=new ArrayList<>();
        if (delValueId!=null){
            //如果valueId参数不为空，说明当前请求中包含属性的参数，每一个属性参数，都会生成一个面包屑
            for (String delValueIds : delValueId) {
                PmsSearchCrumb pmsSearchCrumb =new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueIds);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delValueIds));
                pmsSearchCrumb.setValueName(delValueIds);
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
        }
        modelMap.put("attrValueSelectedList",pmsSearchCrumbs);

        return "list";
    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam,String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam ="";

        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }
        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }
        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueId)){
                    urlParam = urlParam+"&valueId="+pmsSkuAttrValue;
                }
            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam ="";

        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }
        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }
        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam+"&valueId="+pmsSkuAttrValue;
            }
        }

        return urlParam;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }
}
