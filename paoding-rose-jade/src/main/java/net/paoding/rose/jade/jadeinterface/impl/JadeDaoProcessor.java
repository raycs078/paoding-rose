package net.paoding.rose.jade.jadeinterface.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.paoding.rose.jade.jadeinterface.annotation.Dao;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;
import net.paoding.rose.scanning.vfs.FileName;
import net.paoding.rose.scanning.vfs.FileObject;
import net.paoding.rose.scanning.vfs.FileSystemManager;
import net.paoding.rose.scanning.vfs.FileType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class JadeDaoProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    protected static final Log logger = LogFactory.getLog(JadeDaoProcessor.class);

    private static final Pattern DAO_CLASSNAME = Pattern.compile("DAO$", Pattern.CASE_INSENSITIVE);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {

        List<Class<?>> daoClasses;
        try {
            daoClasses = findDaoClasses();
        } catch (IOException e) {
            throw new BeanCreationException("", e);
        }

        DataAccessProvider dataAccessProvider = (DataAccessProvider) applicationContext
                .getBean("jadeDataAccessProviderHolder");
        for (Class<?> daoClass : daoClasses) {
            if (daoClass.isAnnotationPresent(Dao.class) && daoClass.isInterface()) {
                String beanName = daoClass.getName(); // ClassUtils.getShortNameAsProperty(clazz);

                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(DaoFactoryBean.class);
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                propertyValues.addPropertyValue("dataAccessProvider", dataAccessProvider);
                propertyValues.addPropertyValue("daoClass", daoClass);
                beanDefinition.setPropertyValues(propertyValues);
                beanDefinition.setAutowireCandidate(true);

                if (logger.isInfoEnabled()) {
                    logger.info("Generate dao: " + beanName + " ==> " + daoClass.getName());
                }

                DefaultListableBeanFactory defaultBeanFactory = (DefaultListableBeanFactory) beanFactory;
                defaultBeanFactory.registerBeanDefinition(beanName, beanDefinition);
            }
        }
    }

    //------------------

    private List<Class<?>> daoClasses;

    FileSystemManager fsManager = new FileSystemManager();

    public synchronized List<Class<?>> findDaoClasses() throws IOException {

        if (daoClasses == null) {
            daoClasses = new ArrayList<Class<?>>();

            RoseScanner daoScanner = RoseScanner.getInstance();
            List<ResourceRef> resources = new ArrayList<ResourceRef>();
            resources.addAll(daoScanner.getClassesFolderResources());
            resources.addAll(daoScanner.getJarResources());

            for (ResourceRef resourceInfo : resources) {
                if (resourceInfo.hasModifier("dao") || resourceInfo.hasModifier("DAO")) {
                    Resource resource = resourceInfo.getResource();
                    File resourceFile = resource.getFile();
                    FileObject rootObject = null;
                    if (resourceFile.isFile()) {
                        String path = "jar:file:" + resourceFile.getAbsolutePath() + "!/";
                        rootObject = fsManager.resolveFile(path);
                    } else if (resourceFile.isDirectory()) {
                        rootObject = fsManager.resolveFile(resourceFile.getAbsolutePath());
                    }
                    if (rootObject != null) {
                        deepScanImpl(rootObject, rootObject);
                    }
                }
            }
        }

        return new ArrayList<Class<?>>(daoClasses);
    }

    protected void deepScanImpl(FileObject rootObject, FileObject fileObject) {
        try {
            if (!fileObject.getType().equals(FileType.FOLDER)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("fileObject shoud be a folder", // NL
                            new IllegalArgumentException());
                }
                return;
            }

            if ("dao".equals(fileObject.getName().getBaseName())) {
                handleWithFolder(rootObject, fileObject, fileObject);
            } else {
                FileObject[] children = fileObject.getChildren();
                for (FileObject child : children) {
                    if (child.getType().equals(FileType.FOLDER)) {
                        deepScanImpl(rootObject, child);
                    }
                }
            }

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder,
            FileObject thisFolder) throws IOException {

        if (logger.isInfoEnabled()) {
            logger.info("Found dao folder: " + thisFolder);
        }

        FileObject[] children = thisFolder.getChildren();

        // 分两个循环，先处理类文件，再处理子目录，使日志更清晰
        for (FileObject child : children) {
            if (!child.getType().equals(FileType.FOLDER)) {
                handleDAOResource(rootObject, child);
            }
        }
        for (FileObject child : children) {
            if (child.getType().equals(FileType.FOLDER)) {
                handleWithFolder(rootObject, matchedRootFolder, child);
            }
        }
    }

    protected void handleDAOResource(FileObject rootObject, FileObject resource) throws IOException {
        FileName fileName = resource.getName();
        String bn = fileName.getBaseName();
        if (bn.endsWith(".class") && (bn.indexOf('$') == -1)) {
            addDAOClass(rootObject, resource);
        }
    }

    private void addDAOClass(FileObject rootObject, FileObject resource) throws IOException {
        String className = rootObject.getName().getRelativeName(resource.getName());
        className = StringUtils.removeEnd(className, ".class");
        className = className.replace('/', '.');
        if (className.charAt(0) == '.') {
            // BUGFIX: 在某些情况下, 会返回以 '/' 起始的相对路径
            className = className.substring(1);
        }
        for (int i = daoClasses.size() - 1; i >= 0; i--) {
            Class<?> clazz = daoClasses.get(i);
            if (clazz.getName().equals(className)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Skip duplicated class " + className // NL
                            + " in: " + resource);
                }
                return;
            }
        }
        if (!DAO_CLASSNAME.matcher(className).find()) {
            // 忽略名称不匹配的类, 防止这些 .class 被加载。
            if (logger.isDebugEnabled()) {
                logger.debug("Skip class " + className // NL
                        + " in: " + resource);
            }
            return;
        }
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Dao.class)) {
                daoClasses.add(clazz);
                if (logger.isInfoEnabled()) {
                    logger.info("Found class: " + className);
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: " + className, e);
        }
    }
}
