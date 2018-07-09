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
    private Logger logger = Logger.getLogger("MYLOG");

    public MyServicePlugin() {
    }

    /**
     * 获取 。
     *
     * @return {@link #targetProject}
     */
    public String getTargetProject() {
        return targetProject;
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

    /**
     * to generate service by using introspectedTable
     *
     * @param introspectedTable {@link IntrospectedTable}
     * @return {@link Interface}
     */
    private Interface generateServiceInterface(IntrospectedTable introspectedTable) {

        String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        if (validate(null) && StringUtils.isNotEmpty(domainObjectName)) {
            String qualifiedName = serviceTargetPackage + "." + domainObjectName + "Service";
            Interface serviceInterface = new Interface(qualifiedName);

            // visibility
            serviceInterface.setVisibility(JavaVisibility.PUBLIC);

            //doc line
            serviceInterface.addJavaDocLine("/**");
            serviceInterface.addJavaDocLine("* basic service of " + domainObjectName + ".");
            serviceInterface.addJavaDocLine("*/");

            return serviceInterface;
        }
        return null;
    }

    private TopLevelClass generateServiceImplementClass(IntrospectedTable introspectedTable, Interface serviceInterface) {

        if (validate(null) && serviceInterface != null) {
            String qualifiedName = serviceInterface.getType().getShortName() + "Impl";
            TopLevelClass serviceImplementClass = new TopLevelClass(qualifiedName);
            serviceImplementClass.addSuperInterface(serviceInterface.getType());

            serviceImplementClass.addAnnotation("@Service");


            serviceImplementClass.addJavaDocLine("/**");
            serviceImplementClass.addJavaDocLine("* implementation of " + serviceInterface.getType());
            serviceImplementClass.addJavaDocLine("*/");

            Field mapperField = new Field("mapper", new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType()));
            mapperField.setVisibility(JavaVisibility.PRIVATE);
            mapperField.addAnnotation("@Autowired");
            mapperField.addJavaDocLine("/**");
            mapperField.addJavaDocLine("* mapper 层支持。");
            mapperField.addJavaDocLine("*/");

            serviceImplementClass.addImportedType("org.springframework.stereotype.Service");
            serviceImplementClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");

            return serviceImplementClass;
        }

        return null;
    }
}