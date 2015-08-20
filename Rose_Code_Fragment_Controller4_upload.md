本页面展示上传的用法



# 对html表单的要求 #

  * 在页面上，上传表单要写上enctype="multipart/form-data",method="POST" 两个属性
  * 要有type为file的input用于选择文件，file类型的input个数不限，但每个input一定要不同的name

# 在控制器方法中使用MultipartFile #

## 接收name为"file"的input上传上来的文件 ##

```
// MultipartFile是一个Spring类
public String upload(＠Param("file") MultipartFile file) {
    // MultiPartFile提供了getName(), getInputStream(), transferTo()等方法可以方便使用
    return "@ok-"+ file.getName();
}
```

## 接收name以"file"开始的input上传上来的所有文件 ##

```
// 接收名字为file1、file2等等的input的
// files可以是一个数组或者List
public String upload(＠Param("file") MultipartFile[] files) {
    return "@ok-" + Arrays.toString(files);
}
```

## 接收所有文件 ##
```
// 不声明@Param
// files可以是一个数组或者List
public String upload(MultipartFile[] files) {
    return "@ok-" + Arrays.toString(files);
}

// 不声明@Param
// 如果不声明为数组或List，只是一个MultipartFile,则取"第一个"上传的文件(特别适合于只上传一个文件的情况)
public String upload(MultipartFile file) {
    return "@ok-" + file.getName();
}
```

## 混合模式 ##
```
// 接收名字为audio、video的
public String upload(＠Param("audio") MultipartFile audio,＠Param("video") MultipartFile video) {
    return "@ok-";
}
```

```
// 接收名字为audio、video开始的
public String upload(＠Param("audio") MultipartFile[] audio,＠Param("video") MultipartFile[] video) {
    return "@ok-";
}
```


```
// 接收名字为audio的，以及以video开始的
public String upload(＠Param("audio") MultipartFile audio,＠Param("video") MultipartFile[] video) {
    return "@ok-";
}
```