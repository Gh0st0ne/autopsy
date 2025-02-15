/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014-2021 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.autopsy.ingest;

import java.util.Objects;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * A file ingest task that will be executed by an ingest thread using a given
 * ingest job pipeline.
 */
final class FileIngestTask extends IngestTask {

    private final long fileId;
    private AbstractFile file;

    /**
     * Constructs a file ingest task that will be executed by an ingest thread
     * using a given ingest job pipeline.
     *
     * @param ingestJobPipeline The ingest job pipeline to use to execute the
     *                          task.
     * @param file              The file to be processed.
     */
    FileIngestTask(IngestJobPipeline ingestJobPipeline, AbstractFile file) {
        super(ingestJobPipeline);
        this.file = file;
        fileId = file.getId();
    }

    /**
     * Constructs a file ingest task that will be executed by an ingest thread
     * using a given ingest job pipeline. This constructor supports streaming
     * ingest by deferring the construction of the AbstractFile object for this
     * task to conserve heap memory.
     *
     * @param ingestJobPipeline The ingest job pipeline to use to execute the
     *                          task.
     * @param fileId            The object ID of the file to be processed.
     */
    FileIngestTask(IngestJobPipeline ingestJobPipeline, long fileId) {
        super(ingestJobPipeline);
        this.fileId = fileId;
    }

    /**
     * Gets the object ID of the file for this task.
     *
     * @return The object ID.
     */
    long getFileId() {
        return fileId;
    }

    /**
     * Gets the file for this task.
     *
     * @return The file.
     *
     * @throws TskCoreException The exception is thrown if there is an error
     *                          retieving the file from the case database.
     */
    synchronized AbstractFile getFile() throws TskCoreException {
        if (file == null) {
            file = Case.getCurrentCase().getSleuthkitCase().getAbstractFileById(fileId);
        }
        return file;
    }

    @Override
    void execute(long threadId) {
        super.setThreadId(threadId);
        getIngestJobPipeline().execute(this);
    }        
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileIngestTask other = (FileIngestTask) obj;
        IngestJobPipeline thisPipeline = getIngestJobPipeline();
        IngestJobPipeline otherPipeline = other.getIngestJobPipeline();
        if (thisPipeline != otherPipeline && (thisPipeline == null || !thisPipeline.equals(otherPipeline))) {
            return false;
        }
        return (this.fileId == other.fileId);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(getIngestJobPipeline());
        hash = 47 * hash + Objects.hashCode(this.fileId);
        return hash;
    }

}
