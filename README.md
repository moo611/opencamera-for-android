# OpenCamera
一款具有实时滤镜，美颜功能的相机。内置十几款滤镜，可实现拍照，录像功能。

# 效果图
![实时滤镜](https://github.com/moo611/OpenCamera/blob/master/images/tu1.jpg "实时滤镜")
![生成mp4](https://github.com/moo611/OpenCamera/blob/master/images/tu3.gif "生成mp4")

# 功能
#### 1.实时滤镜
#### 2.拍照
#### 3.录制短视频
#### 4.美颜磨皮
#### 
# 使用
#### 集成项目
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
dependencies 
        {
	  implementation 'com.github.moo611:opencamera:1.0.4'
	}
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
 private boolean mRecordingEnabled = false;  // 录制状态
   ...
       mRecordingEnabled = !mRecordingEnabled;
       mCameraView.queueEvent(new Runnable() {
           @Override public void run() {
               // notify the renderer that we want to change the encoder's state
               mCameraView.changeRecordingState(mRecordingEnabled);
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


# 项目比较
|       | 多种滤镜  | 拍照  | 录制视频  |  是否维护  |
|------| ------------ | ------------ | ------------ | ------------ |
|grafika|   X |X  |  √ | X |
| gpuimage | √ | √ | X |X|
|magiccamera| √  |  X | X  |X|
|opencamera|  √ | √   | √   |√ |

### 项目借鉴了不少前辈们的作品，喜欢的麻烦点个赞，谢谢。
#### google/grafika 
https://github.com/google/grafika
#### android gpuimage
https://github.com/cats-oss/android-gpuimage
#### magic camera
https://github.com/wuhaoyu1990/MagicCamera

