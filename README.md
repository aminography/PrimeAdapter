# PrimeAdapter :zap:
[![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-PrimeAdapter-ffaa00.svg?style=flat )]( https://android-arsenal.com/details/1/?)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)
[![Download](https://api.bintray.com/packages/aminography/maven/PrimeAdapter/images/download.svg) ](https://bintray.com/aminography/maven/PrimeAdapter/_latestVersion)
  
**PrimeAdapter** makes working with `RecyclerView` easier by separating needed codes in some simple clean classes.
It brings you simplicity when you have multiple view types in a `RecyclerView`.
You can use **PrimeAdapter** in both [Kotlin](https://github.com/aminography/PrimeAdapter/tree/master/sample-app) and [Java](https://github.com/aminography/PrimeAdapter/tree/master/java-sample-app) Android projects as the sample apps are written.
  
![](static/prime_logo.png)
  
Download
--------
Add the following lines to your `build.gradle` file:
```gradle
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
apply plugin: 'kotlin-kapt'
  
repositories {
    jcenter()
}
  
dependencies {
    implementation 'com.aminography:primeadapter:1.0.2'
    compileOnly 'com.aminography:primeadapter-annotation:1.0.2'
    kapt 'com.aminography:primeadapter-processor:1.0.2'
}
```

* If you write code in Java, you should also add kotlin dependency too:
```gradle
dependencies {
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.70'
}
```
  
How to use PrimeAdapter?
--------
  
You should create both **data holder** and **view holder** classes for each type of view that you want to show in `RecyclerView`.
1. It's necessary to add [`@DataHolder`](https://github.com/aminography/PrimeAdapter) annotation above all data holder classes which inherits from `PrimeDataHolder`:

```kotlin
@DataHolder
data class ActorDataHolder(
        val name: String
) : PrimeDataHolder()
```
 
2. Each view holder class should inherit from `PrimeViewHolder` and specify related data holder as a type parameter like following:

```kotlin
class ActorViewHolder(
        delegate: PrimeDelegate
) : PrimeViewHolder<ActorDataHolder>(delegate, R.layout.list_item) {
  
    override fun bindDataToView(dataHolder: ActorDataHolder) {
        with(itemView) {
            nameTextView.text = dataHolder.name
        }
    }
}
```
  
3. Your custom adapter class must inherit from `PrimeAdapter` that decides to make view holder instance according to appropriate data holder.
Follow this pattern:

```kotlin
class ActorAdapter : PrimeAdapter() {
  
    override fun makeViewHolder(dataHolderClass: Class<*>?): PrimeViewHolder<*>? {
        return when (dataHolderClass) {
            ActorDataHolder::class.java -> ActorViewHolder(this)
            else -> null
        }
    }
}
```

4. Finally, you can instantiate your custom adapter using `PrimeAdapter` builder mechanism.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
      
    val adapter = PrimeAdapter.with(recyclerView)
                    .setLayoutManager(LinearLayoutManager(activity))
                    .set()
                    .build(ActorAdapter::class.java)
      
    val dataList = mutableListOf<PrimeDataHolder>()
    dataList.add(ActorDataHolder("Tom Hanks"))
    dataList.add(ActorDataHolder("Morgan Freeman"))
    dataList.add(ActorDataHolder("Robert De Niro"))
    adapter.replaceDataList(dataList)
}
```

Extra Features
--------

### Handling Item Clicks
**PrimeAdapter** helps you handle `RecyclerView` items click events,
simply by calling `setItemClickListener()` with an `OnRecyclerViewItemClickListener` argument when you're building an adapter instance or later:
```kotlin
val adapter = PrimeAdapter.with(recyclerView)
                ...
                .setItemClickListener(onRecyclerViewItemClickListener)
                ...
                .build(ActorAdapter::class.java)
                
...
adapter.setItemClickListener(onRecyclerViewItemClickListener)
```

### Draggability
**PrimeAdapter** helps you make the `RecyclerView` items draggable.
It would be activated/deactivated by calling `setDraggable(true)`/`setDraggable(false)` on builder or adapter instance.
If you want to get notified about item movements, it's possible by calling `setItemDragListener()` and passing an `OnRecyclerViewItemDragListener` instance to it.

```kotlin
adapter.setDraggable(true)
adapter.setItemDragListener(onRecyclerViewItemDragListener)
```

### Expandability
**PrimeAdapter** helps you make the `RecyclerView` items expandable,
simply by calling `setExpandable(true)` when you're building an adapter instance or later:

```kotlin
adapter.setExpandable(true)
```

### Custom Skip Divider
**PrimeAdapter** helps you make the `RecyclerView` items expandable,
simply by calling `setExpandable(true)` when you're building an adapter instance or later:

```kotlin
val adapter = PrimeAdapter.with(recyclerView)
                ...
                .setDivider() // or `setDivider(null)` for deactivation
                ...
                .build(ActorAdapter::class.java)
                
...
adapter.setDivider() // or pass `setDivider(null)` for deactivation
```
By default dividers are shown for all items except the last one.
It's easy to hide an item's divider by setting `hasDivider` property to `false` on its data holder instance.

Too learn more, see the [wiki][1].

License
--------
```
Copyright 2018 Mohammad Amin Hassani.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[1]: https://github.com/aminography/PrimeAdapter/wiki
