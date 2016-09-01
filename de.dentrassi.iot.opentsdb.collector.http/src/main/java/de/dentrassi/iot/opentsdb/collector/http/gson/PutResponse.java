/*
 * Copyright (C) 2016 Jens Reimann <jreimann@redhat.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dentrassi.iot.opentsdb.collector.http.gson;

public class PutResponse {
    private int success;

    private int failed;

    public int getSuccess() {
        return this.success;
    }

    public void setSuccess(final int success) {
        this.success = success;
    }

    public int getFailed() {
        return this.failed;
    }

    public void setFailed(final int failed) {
        this.failed = failed;
    }
}
