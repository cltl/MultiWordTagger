How to build single jar for mwtagger:

1. unpack kaf.jar and pipelinbuilder.jar
 
jar  xf kaf.jar
jar  xf pipelinebuilder.jar

2. merge the classes with the classes from mwtagger compile output

3. pack jars with manifest

jar -cfm mwtagger.jar ../mwtaggermanifest *

