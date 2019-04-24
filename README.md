# JacquardToolkit for Android

[![](https://jitpack.io/v/gt-etextiles-hacking/JacquardAndroid.svg)](https://jitpack.io/#gt-etextiles-hacking/JacquardAndroid)

The JacquardToolkit library allows directly interfacing with the Google & Levi's Jacquard Jacket via an Android app

## Installation

This library is available via JitPack. Follow the steps below to add the library to your Android project:

1. Add the JitPack repository to your app's dependencies by adding the following to your *root* `build.gradle` file:

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

2. Add the JacquardToolkit dependency by adding the following code to your *app's* `build.gradle` file:

```
dependencies {
	implementation 'com.github.gt-etextiles-hacking.JacquardAndroid:jacquardtoolkit:1.0'
}
```

## Development

Provided in this repository is a demo app that can be used to understand how to utilize the library. To connect to the jacket, simply create a `JacquardJacket` object and follow the example of the demo app provided.

## E-Textile Hacking

This framework is just a small part of the project that our team at Georgia Tech has been working on. If you are interested in learning more about our project, please visit our [Medium publication](https://medium.com/e-textile-hacking).


# License
MIT