package net.paoding.rose.jade.jadeinterface.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.paoding.rose.jade.jadeinterface.annotation.Dao;
import net.paoding.rose.jade.jadeinterface.impl.scanner.DAOScanner;
import net.paoding.rose.jade.jadeinterface.impl.scanner.ResourceInfo;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
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
import org.springframework.util.ClassUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class JadeDaoProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        List<Class<?>> classes;
        try {
            classes = findDaoClasses();
        } catch (IOException e) {
            throw new BeanCreationException("", e);
        }
        DataSourceFactory dataSourceFactory;
        if (applicationContext.containsBean("dataSourceFactory")) {
            dataSourceFactory = (DataSourceFactory) applicationContext.getBean("dataSourceFactory");
        } else {
            dataSourceFactory = (DataSourceFactory) applicationContext
                    .getBean("defaultDataSourceFactory");
        }
        DataAccessProvider dataAccessProvider = (DataAccessProvider) applicationContext
                .getBean("dataAccessProvider");
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Dao.class) && clazz.isInterface()) {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(DaoFactoryBean.class);
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                propertyValues.addPropertyValue("dataSourceFactory", dataSourceFactory);
                propertyValues.addPropertyValue("dataAccessProvider", dataAccessProvider);
                propertyValues.addPropertyValue("daoClass", clazz);
                beanDefinition.setAutowireCandidate(true);
                beanDefinition.setPropertyValues(propertyValues);
                String beanName = ClassUtils.getShortNameAsProperty(clazz);
                System.out.println("------add bean definition " + beanName + "=" + clazz);
                ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition(beanName,
                        beanDefinition);
            }
        }
    }

    //------------------

    protected Log logger = LogFactory.getLog(this.getClass());

    private List<Class<?>> daoClasses;

    public synchronized List<Class<?>> findDaoClasses() throws IOException {
        if (daoClasses == null) {
            daoClasses = new ArrayList<Class<?>>();
            DAOScanner roseScanner = DAOScanner.getRoseScanner();
            List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
            resources.addAll(roseScanner.getClassesFolderResources());
            resources.addAll(roseScanner.getJarResources());
            List<FileObject> rootObjects = new ArrayList<FileObject>();
            FileSystemManager fsManager = VFS.getManager();
            for (ResourceInfo resourceInfo : resources) {
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
                    if (rootObject == null) {
                        continue;
                    }
                    rootObjects.add(rootObject);
                    try {
                        deepScanImpl(rootObject, rootObject);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return new ArrayList<Class<?>>(daoClasses);
    }

    protected void deepScanImpl(FileObject rootObject, FileObject fileObject) {
        try {
            System.out.println("===" + fileObject);
            if (!fileObject.getType().equals(FileType.FOLDER)) {
                logger.warn("fileObject shoud be a folder", new IllegalArgumentException());
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
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder,
            FileObject thisFolder) throws IOException {
        logger.info("found dao folder in " + thisFolder);
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

    protected void handleDAOResource(FileObject rootObject, FileObject resource)
            throws FileSystemException {
        FileName fileName = resource.getName();
        String bn = fileName.getBaseName();
        if (bn.endsWith(".class") && bn.indexOf('$') == -1) {
            addDAOClass(rootObject, resource);
        }
    }

    private void addDAOClass(FileObject rootObject, FileObject resource) throws FileSystemException {
        String className = rootObject.getName().getRelativeName(resource.getName());
        className = StringUtils.removeEnd(className, ".class");
        className = className.replace('/', '.');
        for (int i = daoClasses.size() - 1; i >= 0; i--) {
            Class<?> clazz = daoClasses.get(i);
            if (clazz.getName().equals(className)) {
                logger
                        .info("dao: skip replicated class " + className + " in "
                                + resource.getName());
                return;
            }
        }
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Dao.class)) {
                daoClasses.add(clazz);
                logger.info("dao: found class, name=" + className);
            }
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }
}
