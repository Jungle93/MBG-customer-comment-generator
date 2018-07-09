package com.jungle.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * @author jungle
 * @version V1.0
 * @date 2018/7/9 13:14
 * @Title: MapperAnnotationPlugin.java
 * @Package com.jungle.plugin
 * @Description: Mapper注解
 * copyright © 2018- jungle.com
 */
public class MapperAnnotationPlugin extends PluginAdapter {


    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        interfaze.addJavaDocLine("/**");
        interfaze.addJavaDocLine("*" + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + "的映射接口。");
        interfaze.addJavaDocLine("*/");

        interfaze.addAnnotation("@Mapper");
        interfaze.addAnnotation("@Repository");
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        interfaze.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Repository"));

        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

}
