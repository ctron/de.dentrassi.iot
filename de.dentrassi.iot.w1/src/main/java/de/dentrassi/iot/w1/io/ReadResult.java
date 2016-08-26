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

package de.dentrassi.iot.w1.io;

import java.time.Instant;
import java.util.Objects;

public class ReadResult<T> {
    private final T data;
    private final Throwable error;
    private final Instant timestamp = Instant.now();

    public ReadResult(final T data) {
        this.data = data;
        this.error = null;
    }

    public ReadResult(final Throwable e) {
        Objects.requireNonNull(e);

        this.data = null;
        this.error = e;
    }

    public T getData() {
        if (this.error != null) {
            throw new RuntimeException("Result is error", this.error);
        }
        return this.data;
    }

    public Throwable getError() {
        return this.error;
    }

    @Override
    public String toString() {
        final String timestamp = this.timestamp.toString();

        if (this.error == null) {
            return timestamp + " " + this.data.toString();
        } else {
            String message = this.error.getMessage();
            if (message == null) {
                message = this.error.getClass().getName();
            }
            return String.format("%s [ErrorResult: %s]", timestamp, message);
        }
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
}