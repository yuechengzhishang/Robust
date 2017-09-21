
# Robust Keep Resource Id Plugin
Keep Resource Id Plugin can be used absolutely

# Usage

1. Add below codes in the module's build.gradle.

	```java
	apply plugin: 'keep-resource-id'
	```
2. Add below codes in the outermost project's build.gradle file.

	```java
	 buildscript {
	    repositories {
	        jcenter()
	    }
	    dependencies {
	         classpath 'com.meituan.robust:keep-resource-id:0.7.30'
	   }
	}
	```
3. put your R.txt in the outermost project's robust dir , R.txt path is like 'app/build/intermediates/symbols/release/R.txt'

4. enjoy yourself



