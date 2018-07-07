package com.jungle.plugin;

import com.jungle.util.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MyServicePlugin extends PluginAdapter {

    private String targetProject;
    private String serviceTargetPackage;
    private String serviceImplementPackage;
    private ShellCallback shellCallback;
    private Logger logger = Logger.getLogger("MYLOG");

    public MyServicePlugin() {
        shellCallback = new DefaultShellCallback(true);
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

    @Override
    public boolean validate(List<String> list) {
        targetProject = properties.getProperty("serviceTargetProject");
        serviceTargetPackage = properties.getProperty("serviceTargetPackage");
        serviceImplementPackage = properties.getProperty("serviceImplementPackage");
        return StringUtils.isNotEmpty(targetProject) && StringUtils.isNotEmpty(serviceTargetPackage) && StringUtils.isNotEmpty(serviceImplementPackage);
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {

        /*
        步骤：
        1、生成Service Interface
        2、生成ServiceImpl
        3、为ServiceImpl添加Mapper字段
         */
        List<GeneratedJavaFile> serviceJavaFiles = new ArrayList<GeneratedJavaFile>();
        String beanName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        JavaFormatter javaFormatter = context.getJavaFormatter();
        if (StringUtils.isNotEmpty(beanName) && StringUtils.isNotEmpty(serviceTargetPackage)) {
            //[step.1] add service interface
            Interface serviceInterface = new Interface(
                    serviceTargetPackage + "." + beanName + "Service");
            serviceInterface.setVisibility(JavaVisibility.PUBLIC);
            serviceInterface.addJavaDocLine("/**");
            serviceInterface.addJavaDocLine(" * " + beanName + "服务层。");
            serviceInterface.addJavaDocLine(" */");
            GeneratedJavaFile serviceJavaFile = new GeneratedJavaFile(serviceInterface, targetProject, "utf-8", javaFormatter);
            //[step.2] add service implement
            FullyQualifiedJavaType serviceImplType = new FullyQualifiedJavaType(serviceImplementPackage + "." + serviceInterface.getType().getShortName() + "Impl");
            //[step.3] add service implement mapper field
            TopLevelClass serviceImplementJavaFile = new TopLevelClass(serviceImplType);
            serviceImplementJavaFile.addAnnotation("@Service");
            serviceImplementJavaFile.addSuperInterface(serviceInterface.getType());
            serviceImplementJavaFile.addImportedType("org.springframework.stereotype.Service");
            serviceImplementJavaFile.addImportedType("org.springframework.beans.factory.annotation.Autowired");
            serviceImplementJavaFile.addJavaDocLine("/**");
            serviceImplementJavaFile.addJavaDocLine("*" + serviceInterface.getType().getShortName() + "的实现类。");
            serviceImplementJavaFile.addJavaDocLine("*/");
            Field mapper = new Field("mapper", new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType()));
            mapper.setVisibility(JavaVisibility.PRIVATE);
            mapper.addAnnotation("@Autowired");
            mapper.addJavaDocLine("/**");
            mapper.addJavaDocLine("* Mapper 层支持。");
            mapper.addJavaDocLine("*/");
            serviceImplementJavaFile.addField(mapper);
            GeneratedJavaFile serviceImplJavaFile = new GeneratedJavaFile(serviceImplementJavaFile, targetProject, "utf-8", javaFormatter);
            serviceJavaFiles.add(serviceJavaFile);
            serviceJavaFiles.add(serviceImplJavaFile);
        }


        return serviceJavaFiles;
    }
}