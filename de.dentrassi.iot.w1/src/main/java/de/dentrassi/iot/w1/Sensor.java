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

package de.dentrassi.iot.w1;

public class Sensor {
    private final String master;
    private final String slave;

    public Sensor(final String master, final String slave) {
        this.master = master;
        this.slave = slave;
    }

    public String getMaster() {
        return this.master;
    }

    public String getSlave() {
        return this.slave;
    }

    public String getMasterAndSlave() {
        return String.format("%s/%s", this.master, this.slave);
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", this.master, this.slave);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.master == null ? 0 : this.master.hashCode());
        result = prime * result + (this.slave == null ? 0 : this.slave.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sensor other = (Sensor) obj;
        if (this.master == null) {
            if (other.master != null) {
                return false;
            }
        } else if (!this.master.equals(other.master)) {
            return false;
        }
        if (this.slave == null) {
            if (other.slave != null) {
                return false;
            }
        } else if (!this.slave.equals(other.slave)) {
            return false;
        }
        return true;
    }
}