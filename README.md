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
dependencies 
        {
	  implementation 'com.github.moo611:OpenCamera:1.0.1'
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


# 项目比较
### 在这里首先向前辈们致敬！只是晚辈在使用上述框架的过程中出现了很多问题，因此才想做一个更加完善的解决方案。如果大家在使用过程中有什么问题，或者有什么好的建议，欢迎留言。喜欢的麻烦点个赞，谢谢。
#### google/grafika 
https://github.com/google/grafika
#### android gpuimage
https://github.com/cats-oss/android-gpuimage
#### magic camera
https://github.com/wuhaoyu1990/MagicCamera

|       | 多种滤镜  | 拍照  | 录制视频  |  是否维护  |
|------| ------------ | ------------ | ------------ | ------------ |
|grafika|   X |X  |  √ | X |
| gpuimage | √ | √ | X |X|
|magiccamera| √  |  X | X  |X|
|opencamera|  √ | √   | √   |√ |

