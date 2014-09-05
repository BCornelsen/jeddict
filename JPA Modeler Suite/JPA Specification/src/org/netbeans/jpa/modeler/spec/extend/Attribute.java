/**
 * Copyright [2014] Gaurav Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.netbeans.jpa.modeler.spec.extend;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Gaurav Gupta
 */
public abstract class Attribute extends FlowPin {

    private List<String> annotation;
    @XmlAttribute
    private boolean visibile = true;

    /**
     * @return the annotation
     */
    public List<String> getAnnotation() {
        if (annotation == null) {
            annotation = new ArrayList<String>();
        }
        return annotation;
    }

    /**
     * @param annotation the annotation to set
     */
    public void setAnnotation(List<String> annotation) {
        this.annotation = annotation;
    }

    public void addAnnotation(String annotation_In) {
        if (annotation == null) {
            annotation = new ArrayList<String>();
        }
        this.annotation.add(annotation_In);
    }

    public void removeAnnotation(String annotation_In) {
        if (annotation == null) {
            annotation = new ArrayList<String>();
        }
        this.annotation.remove(annotation_In);
    }

    /**
     * @return the visibile
     */
    public boolean isVisibile() {
        return visibile;
    }

    /**
     * @param visibile the visibile to set
     */
    public void setVisibile(boolean visibile) {
        this.visibile = visibile;
    }

}
