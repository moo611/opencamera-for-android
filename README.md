# OpenCamera
一款具有实时滤镜，美颜功能的相机。内置十几款滤镜，可实现拍照，录像功能。

# 效果图
![实时滤镜](https://github.com/moo611/OpenCamera/blob/master/images/tu1.jpg "实时滤镜")
![生成mp4](https://github.com/moo611/OpenCamera/blob/master/images/tu3.gif "生成mp4")

# 功能
#### 1.实时滤镜
#### 2.拍照
#### 3.录像
#### 目前仅仅实现以上的基本功能，后期还会维护，添加美颜，水印，特效等功能
# 使用
#### 集成项目
```
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}

dependencies 
        {
	  implementation 'com.github.moo611:OpenCamera:1.0.3'
	}
//注意添加java8支持！！！
android{
...
 compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }
}
	
```
#### 添加权限
```
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.CAMERA
```    
#### xml布局文件
```xml
 <com.atech.glcamera.views.GLCameraView
        app:layout_constraintTop_toTopOf="parent"
        android:id="@+id/glcamera"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
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
#### 设置输出mp4文件
```java
 mCameraView.setOuputMP4File(your file);
```
#### 录制视频
```java
 private boolean mRecordingEnabled = false;  // 录制状态
   ...
       mRecordingEnabled = !mRecordingEnabled;
       mCameraView.changeRecordingState(mRecordingEnabled);
```
#### 设置mp4录制完成回调
```java
 mCameraView.setrecordFinishedListnener(new FileCallback() {
            @Override
            public void onData(File file) {

                //update the gallery
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));

            }
        });
```
#### home键切出时终止录屏(可选)
@Override
    protected void onStop() {
        super.onStop();

        mRecordingEnabled = false;
        mCameraView.changeRecordingState(mRecordingEnabled);
    }

# 项目比较
|       | 多种滤镜  | 拍照  | 录制视频  |  是否维护  |
|------| ------------ | ------------ | ------------ | ------------ |
|grafika|   X |X  |  √ | X |
| gpuimage | √ | √ | X |X|
|magiccamera| √  |  X | X  |X|
|opencamera|  √ | √   | √   |√ |

### 首先向前辈们表示尊敬和感谢！项目借鉴了很多前辈们的作品。项目还有许多不足之处，欢迎大家批评指正。
#### google/grafika 
https://github.com/google/grafika
#### android gpuimage
https://github.com/cats-oss/android-gpuimage
#### magic camera
https://github.com/wuhaoyu1990/MagicCamera

