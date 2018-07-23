package com.jungle.plugin;

import com.jungle.util.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

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

    /**
     * validate if configuration of the plugin is correct.
     *
     * @param list List String
     * @return boolean
     */
    @Override
    public boolean validate(List<String> list) {
        targetProject = properties.getProperty("serviceTargetProject");
        serviceTargetPackage = properties.getProperty("serviceTargetPackage");
        serviceImplementPackage = properties.getProperty("serviceImplementPackage");
        return StringUtils.isNotEmpty(targetProject) && StringUtils.isNotEmpty(serviceTargetPackage) && StringUtils.isNotEmpty(serviceImplementPackage);
    }

    /**
     * create additional Java Files
     *
     * @param introspectedTable {@link IntrospectedTable}
     * @return List {@link GeneratedJavaFile}
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {

        List<GeneratedJavaFile> javaFiles = new ArrayList<GeneratedJavaFile>();
        JavaFormatter javaFormatter = context.getJavaFormatter();

        if (validate(null)) {
            Interface serviceInterface = generateServiceInterface(introspectedTable);
            if (serviceInterface != null) {
                GeneratedJavaFile serviceJavaFile = new GeneratedJavaFile(serviceInterface, targetProject, "utf-8", javaFormatter);
                javaFiles.add(serviceJavaFile);
                TopLevelClass serviceImplementClass = generateServiceImplementClass(introspectedTable, serviceInterface);
                if (serviceImplementClass != null) {
                    GeneratedJavaFile serviceImplementJavaFile = new GeneratedJavaFile(serviceImplementClass, targetProject, "utf-8", javaFormatter);
                    javaFiles.add(serviceImplementJavaFile);
                }
            }
        }
        return javaFiles;
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

    /**
     * generate a service implementation class.
     *
     * @param introspectedTable table
     * @param serviceInterface  service interface
     * @return {@link TopLevelClass}
     */
    private TopLevelClass generateServiceImplementClass(IntrospectedTable introspectedTable, Interface serviceInterface) {

        if (validate(null) && serviceInterface != null) {
            String qualifiedName = serviceImplementPackage+"."+serviceInterface.getType().getShortName() + "Impl";
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
            serviceImplementClass.addField(mapperField);

            serviceImplementClass.addImportedType("org.springframework.stereotype.Service");
            serviceImplementClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");

            return serviceImplementClass;
        }

        return null;
    }
}