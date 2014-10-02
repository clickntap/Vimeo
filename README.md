### Welcome to Java Vimeo API 3.0.
To use this api you’ll first need to register your app from Vimeo:

https://developer.vimeo.com/apps

Then you'll need to generate an Access Token with upload access.
The generated Token is all you need to use the Java Vimeo API 3.0.

```python

package com.clickntap.vimeo;

import java.io.File;

public class VimeoSample {

	public static void main(String[] args) throws Exception {
    Vimeo vimeo = new Vimeo("[token]"); 
    
    //add a video
    
    boolean upgradeTo1080 = true;
    
    String videoEndPoint = vimeo.addVideo(new File("/Users/tmendici/Downloads/Video.AVI"), upgradeTo1080);
    
    //edit video
    String name = "Name";
    String desc = "Description";
    String license = "" //see Vimeo API Documentation
    String privacyView = "nobody"; //see Vimeo API Documentation
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


### Support or Contact
Having trouble with Java Vimeo API 3.0? Contact info@clickntap.com and we’ll help you sort it out.
