/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.common.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 01.10.2015.
 */
@Entity
//@Table(name = "datasetMetadata")
public class DatasetMetadata extends OwnedResource{

    /*public DatasetMetadata(Visibility visibility, String name, String description) {
        super();
        setVisibility(visibility);
        setDescription(description);
        this.name = name;
        totalEntities = 0;
    }
    public DatasetMetadata(User owner, Visibility visibility, String name, String description) {
        super(owner);
        setVisibility(visibility);
        setDescription(description);
        this.name = name;
        totalEntities = 0;
    }*/

    public DatasetMetadata(){super(null);}

    public DatasetMetadata(String name){
        super();
        this.name = name;
        totalEntities = 0;
    }

    @JsonIgnore
    public String getIdentifier(){
        return getName();
    }

    private String name;

    private long totalEntities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalEntities() {
        return totalEntities;
    }

    public void setTotalEntities(long totalEntities) {
        this.totalEntities = totalEntities;
    }

}

