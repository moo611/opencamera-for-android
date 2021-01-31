# OpenCamera
#### opencamera是一套高性能的相机框架，基于opengles+glsurfaceview,能实现实时滤镜，拍照，录制短视频，美颜磨皮等功能。

# 效果图
![实时滤镜](https://github.com/moo611/OpenCamera/blob/master/images/tu1.jpg "实时滤镜")
![生成mp4](https://github.com/moo611/OpenCamera/blob/master/images/tu3.gif "生成mp4")

# 已实现功能
1.拍照
2.录视频
3.美颜，滤镜

# 开发计划
1.加入CameraX Api
2.将glsurfaceview变成自定义的textureview渲染
3.离线渲染
4.将opengl java部分的代码变成可移植的c库，便于ios上的移植。


# 集成
#### 版本号
[![](https://www.jitpack.io/v/moo611/OpenCamera.svg)](https://www.jitpack.io/#moo611/OpenCamera)
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
dependencies 
        {
	  implementation 'com.github.moo611:OpenCamera:latestversion'
	}
//注意在android代码块里添加java8支持！！		
 	
```
#### 需要添加jdk 1.8支持
```gradle
compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }
```

#### 注意，由于demo里用的androidX,因此如果您的项目用的是v4或者v7包，会出现manifest不兼容的情况，有三种解决方案
#### (1)升级您的应用到androidX
#### (2)直接导入module的方式导入我的library
#### (3)用1.0.4及以下版本(不建议)


# 使用

#### 添加权限
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
```
#### xml布局文件
```xml
 <com.atech.glcamera.views.GLCameraView
        app:layout_constraintTop_toTopOf="parent"
        android:id="@+id/glcamera"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
#### 开启或关闭美颜（默认开启）
```java
 mCameraView.enableBeauty(true);
```
#### 美颜程度（0~1）
```java
mCameraView.setBeautyLevel(0.5f);
```
#### 添加滤镜

```java

private List<FilterFactory.FilterType>filters = new ArrayList<>();
  ...
  filters.add(FilterFactory.FilterType.Original);
  filters.add(FilterFactory.FilterType.Sunrise);
  filters.add(FilterFactory.FilterType.Sunset);
  filters.add(FilterFactory.FilterType.BlackWhite);
  filters.add(FilterFactory.FilterType.WhiteCat);
  filters.add(FilterFactory.FilterType.BlackCat);
  filters.add(FilterFactory.FilterType.SkinWhiten);

```

#### 切换滤镜
```java
 mCameraView.updateFilter(filters.get(pos));
```

#### 切换镜头
```java
 mCameraView.switchCamera();
```

#### 拍照

```java
 mCameraView.takePicture(new FilteredBitmapCallback() {
            @Override
            public void onData(Bitmap bitmap) {
                 ...
            }
        });
```

#### 录制视频
```java
 private boolean isRecording = false;  // 录制状态
   ...
    
       btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
		
		isRecording = !isRecording;
       
                mCameraView.changeRecordingState(isRecording);
		
            }
        });

       
         
```

#### 设置视频保存路径及拍摄完成的回调
```java
 
        mCameraView.setOuputMP4File(mFile);
       
        mCameraView.setrecordFinishedListnener(new FileCallback() {
            @Override
            public void onData(File file) {

                //update the gallery
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));

            }
        });
```

### 参考
#### google/grafika 
https://github.com/google/grafika
#### android gpuimage
https://github.com/cats-oss/android-gpuimage
#### magic camera
https://github.com/wuhaoyu1990/MagicCamera

