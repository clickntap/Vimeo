### Welcome to Java Vimeo API 3.0.
To use this api you’ll first need to register your app from Vimeo:

https://developer.vimeo.com/apps

Then you'll need to generate an Access Token with upload access.
The generated Token is all you need to use the Java Vimeo API 3.0.

```java

package com.clickntap.vimeo;

import java.io.File;

public class VimeoSample {

  public static void main(String[] args) throws Exception {
    Vimeo vimeo = new Vimeo("[token]"); 
    
    //add a video
    boolean upgradeTo1080 = true;
    String videoEndPoint = vimeo.addVideo(new File("/Users/tmendici/Downloads/Video.AVI"), upgradeTo1080);
    
    //get video info
    VimeoResponse info = vimeo.getVideoInfo(videoEndPoint);
    System.out.println(info);
    
    //edit video
    String name = "Name";
    String desc = "Description";
    String license = ""; //see Vimeo API Documentation
    String privacyView = "disable"; //see Vimeo API Documentation
    String privacyEmbed = "whitelist"; //see Vimeo API Documentation
    boolean reviewLink = false;
    vimeo.updateVideoMetadata(videoEndPoint, name, desc, license, privacyView, privacyEmbed, reviewLink);
    
    //add video privacy domain
    vimeo.addVideoPrivacyDomain(videoEndPoint, "clickntap.com");
   
    //delete video
    vimeo.removeVideo(videoEndPoint);
    
  }

}


```

The class VideoResponse provides response code and json response, see Vimeo API documentation to check errors.

### Use with Maven

```xml

<dependency>
  <groupId>com.clickntap</groupId>
  <artifactId>vimeo</artifactId>
  <version>1.4</version>
</dependency>
 
```

### Use with Gradle on Android
Be sure to not call network related methods on the main-thread.
```
repositories {
    mavenCentral()
}

dependencies {
  compile 'com.clickntap:vimeo:1.4'
  // as the default Android HttpClient is outdatet add this too (https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html) 
  compile group: 'org.apache.httpcomponents', name: 'httpclient-android', version: '4.3.5.1'
}
```    


### Support or Contact
Having trouble with Java Vimeo API 3.0? Contact info@clickntap.com and we’ll help you sort it out.
