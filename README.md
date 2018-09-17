# PrimeAdapter :boom:
[![API](https://img.shields.io/badge/API-9%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![Download](https://api.bintray.com/packages/aminography/maven/PrimeAdapter/images/download.svg) ](https://bintray.com/aminography/maven/PrimeAdapter/_latestVersion)
  
**PrimeAdapter** makes working with `RecyclerView` easier by separating needed codes in some simple clean classes.
It brings you simplicity when you have multiple view types in a `RecyclerView`.

How to use PrimeAdapter?
--------
  
You should create both **data holder** and **view holder** classes for each type of view that you want to show in `RecyclerView`.
1. It's necessary to add `@DataHolder` annotation above all data holder classes which inherits from `PrimeDataHolder`:

```kotlin
@DataHolder
data class ActorDataHolder(
        val name: String
) : PrimeDataHolder()
```
 
2. Each view holder class should inherits from `PrimeViewHolder` and specify related data holder as type parameter like following:

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
  
3. Your custom adapter class must inherits from `PrimeAdapter` that decides to make view holder instance according to appropriate data holder.
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
    implementation 'com.aminography:primeadapter:1.0.0'
    compileOnly 'com.aminography:primeadapter-annotation:1.0.0'
    kapt 'com.aminography:primeadapter-processor:1.0.0'
}
```

### Using PrimeAdapter in Java Android project
If you write code in Java, you should also add kotlin dependency too:
```gradle
dependencies {
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.70'
}
```

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