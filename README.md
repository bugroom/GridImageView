# GridImageView
## 宫格方式显示ImageView
上面工程我用的是Kotlin写的，不会kotlin也没有关系，方法都一样的

### 效果图

[截图1](https://drny.cc/image/Screenshot_2021-01-17-13-19-27-165_com.yu.gridima.jpg)
[截图2](https://drny.cc/image/Screenshot_2021-01-17-13-19-33-363_com.yu.gridima.jpg)
![截图1](https://drny.cc/image/Screenshot_2021-01-17-13-19-27-165_com.yu.gridima.jpg)
![截图2](https://drny.cc/image/Screenshot_2021-01-17-13-19-33-363_com.yu.gridima.jpg)

### 1.添加如下配置到你的工程中
```groovy
dependencies {
    implementation 'com.gridimageview.yu:gridimageview:1.1.8'
}
```
### 2.布局文件添加代码
```xml
<com.gridimageview.yu.GridImageView
     android:id="@+id/gridImageView"
     android:layout_width="match_parent"
     android:layout_height="wrap_content" />
```
**注意: 宽度属性必须设置为match_parent, 在RecyclerView的Item中使用也一样**

### 3. 添加数据到集合

**Java代码：**
```
List<String> list = new ArrayList<>();
list.add("https://img.tupianzj.com/uploads/allimg/202010/9999/rn2b58e62da0.jpg");
list.add("https://img.tupianzj.com/uploads/allimg/202009/9999/rne452d88a27.jpg");
list.add("https://img.tupianzj.com/uploads/allimg/201911/9999/rna4bf242e64.jpg");
```
**Kotlin代码:**
```kotlin
val imageData = mutableListOf<String>()
imageData.add("图片直链")
```

### 4.设置要加载的图片链接

**Java代码:**
```
GridImageView gridImageView = findViewById(R.id.gridImageView);
gridImageView.setImageUrls(list);
```
**Kotlin代码:**
```kotlin
val gridImageView: GridImageView = findViewById(R.id.gridImageView)
gridImageView.setImageUrls(imageData)
// 参数类型为 List<String>
```
**注意：如果是单张图片显示你得设置图片宽高,如果不设置加载过程中可能显示异常**
```kotlin
gridImageView.setImageViewSize(700, 400)
```
你给出的宽高可以是图片原尺寸，会自动对图片宽高进行处理

### 5.点击事件

**Java代码：**
```
gridImageView.setOnImageItemClickListener(new OnImageItemClickListener() {
       @Override
       public void onImageItemClick(ViewGroup viewGroup, View view, int position) {
           Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
       }
   });
```
**Kotlin代码:**
```kotlin
gridImageView.setOnImageItemClickListener(object : OnImageItemClickListener {
        override fun onImageItemClick(parent: ViewGroup, v: View, position: Int) {
             Toast.makeText(parent.context, position.toString(), Toast.LENGTH_SHORT).show()
        }
    })
// parent gridImageView, v 当前点击的ImageView, position ImageView在View中的位置
```

### 属性介绍

属性 | 使用 | 作用
----|-----|------
isRuleSort|setImageRuleSort|是否进行规则排序,默认为false
imageSpacing|setImageSpacing|设置图片间的间隔
imageCornerRadius|无|设置图片各角的圆角半径
imageBorderWidth|无|设置图片边框宽度
imageBorderColor|无|设置边框颜色
singleViewHandle|无|是否对图片宽高进行处理
singleViewFullWidth|setSingleViewFullWidth|单张图片宽度设置为最大
imagePlaceHolder|setImagePlaceHolder|设置占位图,参数类型为drawable
imageMaxCount|无|最多可展示图片的数量,超出右上角会显示图片实际数量

**注意：如果你能确保你的图片能在布局中完整显示,可以设置singleViewHandle为false**

### 在RecyclerView中显示

> 必须设置setImageViewSize

> 你无需进行任何处理

> 具体实现请查看上面工程

示例代码(Adapter-Kotlin) **具体实现请克隆我的工程** ：
```kotlin
class ImageAdapter(private val mData: MutableList<DongYu>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TextView = itemView.findViewById(R.id.name)
        val content: TextView = itemView.findViewById(R.id.content)
        val gridImageView: GridImageView = itemView.findViewById(R.id.gridImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        val holder = ViewHolder(itemView)
        holder.gridImageView.setOnImageItemClickListener(object : OnImageItemClickListener {
            override fun onImageItemClick(parent: ViewGroup, v: View, position: Int) {
                Toast.makeText(parent.context, position.toString(), Toast.LENGTH_SHORT).show()
            }
        })
        return holder
    }

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mData[position]
        Glide.with(holder.itemView.context).load(data.icon).into(holder.icon)
        holder.name.text = data.name
        holder.content.text = (data.content + data.images.size)
        if (data.images.size == 1) {
             // 记得对单张图片进行设置宽高
             holder.gridImageView.setImageViewSize(700, 400)
        }
        holder.gridImageView.setImageUrls(data.images)
    }
}
```