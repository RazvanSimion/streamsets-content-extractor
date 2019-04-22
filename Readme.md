# How to create a custom streamsets processor for content extraction

We will use Apache Tika for content extraction.


# Prepare streamsets

## Requirements

You must have docker daemon installed

## Start the Streamsets docker image
  
    docker run --restart on-failure -p 18630:18630 -d --name streamsets-dc streamsets/datacollector dc

## Check Streamsets image

http://localhost:18630/

admin/admin


# Generate the custom processor

## Get the appropiate archetype: 3.7.2

https://mvnrepository.com/artifact/com.streamsets/streamsets-datacollector-stage-lib-tutorial

## Generate the project

    mvn archetype:generate -DarchetypeGroupId=com.streamsets -DarchetypeArtifactId=streamsets-datacollector-stage-lib-tutorial -DarchetypeVersion=3.7.2 -DinteractiveMode=true

## Add Tika dependency in pom.xml
    ...
    <dependency>
      <groupId>org.apache.tika</groupId>
      <artifactId>tika-parsers</artifactId>
      <version>1.17</version>
    </dependency>
    ...

## Delete destination, origin, executor

Delete the packages destination, origin, executor from src/main/java/ro.trc.streamsets/stage/
Delete the packages destination, origin, executor from src/test/java/ro.trc.streamsets/stage/

## Change labels

ro.trc.streamsets.stage.processor.sample/SampleDProcessor.java
    ...
    @StageDef(
        version = 1,
        label = "Content Extract Processor",
        description = "",
        icon = "default.png",
        onlineHelpRefUrl = ""
    )
    ...

## Add Apache Tika specific code

ro.trc.streamsets.stage.processor.sample/SampleProcessor.java

    /*
     * Copyright 2017 StreamSets Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package ro.trc.streamsets.stage.processor.sample;
    
    import com.streamsets.pipeline.api.Field;
    import com.streamsets.pipeline.api.FileRef;
    import com.streamsets.pipeline.api.base.OnRecordErrorException;
    import org.apache.tika.exception.TikaException;
    import org.apache.tika.parser.AutoDetectParser;
    import org.apache.tika.parser.ParseContext;
    import org.apache.tika.parser.Parser;
    import org.apache.tika.sax.BodyContentHandler;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.xml.sax.ContentHandler;
    import org.xml.sax.SAXException;
    import ro.trc.streamsets.stage.lib.sample.Errors;
    
    import com.streamsets.pipeline.api.Record;
    import com.streamsets.pipeline.api.StageException;
    import com.streamsets.pipeline.api.base.SingleLaneRecordProcessor;
    
    import java.io.IOException;
    import java.io.InputStream;
    import java.util.HashMap;
    import java.util.List;
    
    import org.apache.tika.metadata.Metadata;
    
    public abstract class SampleProcessor extends SingleLaneRecordProcessor {
        private static final Logger LOG = LoggerFactory.getLogger(SampleProcessor.class);
    
        /**
         * Gives access to the UI configuration of the stage provided by the {@link SampleDProcessor} class.
         */
        public abstract String getConfig();
    
        /**
         * {@inheritDoc}
         */
        @Override
        protected List<ConfigIssue> init() {
            // Validate configuration values and open any required resources.
            List<ConfigIssue> issues = super.init();
    
            if (getConfig().equals("invalidValue")) {
                issues.add(
                        getContext().createConfigIssue(
                                Groups.SAMPLE.name(), "config", Errors.SAMPLE_00, "Here's what's wrong..."
                        )
                );
            }
    
            // If issues is not empty, the UI will inform the user of each configuration issue in the list.
            return issues;
        }
    
        /**
         * {@inheritDoc}
         */
        @Override
        public void destroy() {
            // Clean up any open resources.
            super.destroy();
        }
    
        /**
         * {@inheritDoc}
         */
        @Override
        protected void process(Record record, SingleLaneBatchMaker batchMaker) throws StageException {
    
            FileRef fileRef = record.get("/fileRef").getValueAsFileRef();
    
    
            try {
    
                InputStream inputStream = fileRef.createInputStream(getContext(), InputStream.class);
                String content = extractContentUsingParser(inputStream);
    
                Record newRecord = getContext().createRecord(record);
                HashMap<String, Field> root = new HashMap<>();
                root.put("fileInfo", record.get("/fileInfo"));
                root.put("fileRef", record.get("/fileRef"));
    
                root.put("content", Field.create(content));
    
                newRecord.set("/", Field.create(root));
                batchMaker.addRecord(newRecord);
    
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                throw new OnRecordErrorException(record, Errors.SAMPLE_01, e);
    
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new OnRecordErrorException(record, Errors.SAMPLE_00, e);
            }
        }
    
        public static String extractContentUsingParser(InputStream stream)
                throws IOException, TikaException, SAXException {
    
            Parser parser = new AutoDetectParser();
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
    
            parser.parse(stream, handler, metadata, context);
            return handler.toString();
        }
    }

## Build the project

    mvn clean package -DskipTests

# Install the processor

## Start docker image

    docker run --restart on-failure -p 18630:18630 -d --name streamsets-dc streamsets/datacollector dc

## Find image locations


    docker ps
    docker exec -it d335b61b2bf4   /bin/bash
    
    ps -ef | grep userLibrariesDir 
        - /opt/streamsets-datacollector-user-libs
    
    exit
    
## Copy the package in docker container

    docker ps
    
    docker cp "C:\Users\razva\Documents\Projects\WORKSHOPS\BIG DATA\INGESTION - STREAMSETS\6. STREAMSETS - CUSTOM PLUGIN\streamsets-content-extraction\content-extractor\target\content-extractor-1.0.0.tar.gz" d335b61b2bf4:/
    docker exec -it d335b61b2bf4  /bin/bash
    
    cd /opt/streamsets-datacollector-user-libs
    sudo tar xvfz /content-extractor-1.0.0.tar.gz
    
    exit

## Restart container

    docker ps
    docker container restart d335b61b2bf4
    
# Create a pipeline

Custom content extraction demo

# Run the pipeline

    docker cp C:\streamsets\libs\content-extractor c36d212f9f09:/
    docker exec -it c36d212f9f09 /bin/bash
    sudo cp -R content-extractor /opt/streamsets-datacollector-3.7.2/user-libs/
    exit


References
https://streamsets.com/
https://hub.docker.com/r/streamsets/datacollector
https://github.com/streamsets/tutorials/blob/master/tutorial-processor/readme.md
https://mvnrepository.com/artifact/com.streamsets/streamsets-datacollector-stage-lib-tutorial
https://tika.apache.org/
https://www.baeldung.com/apache-tika


