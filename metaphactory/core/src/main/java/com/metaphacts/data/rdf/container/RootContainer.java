/*
 * Copyright (C) 2015-2017, metaphacts GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, you can receive a copy
 * of the GNU Lesser General Public License from http://www.gnu.org/
 */

package com.metaphacts.data.rdf.container;

import java.util.Collections;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.google.common.collect.Sets;
import com.metaphacts.data.rdf.PointedGraph;
import com.metaphacts.vocabulary.LDP;

/**
 * Representation of the platform root container.
 * @author Johannes Trame <jt@metaphacts.com>
 *
 */
@LDPR(iri=RootContainer.IRI_STRING)
public class RootContainer extends AbstractLDPContainer {
    public static final String IRI_STRING = "http://www.metaphacts.com/ontologies/platform#rootContainer";
    public static final IRI IRI = vf.createIRI(IRI_STRING);
    
    public RootContainer(IRI iri, Repository repository) {
        super(iri, repository);
    }
    
    public RootContainer(Repository repository) {
        super(IRI, repository);
    }
    
    @Override
    public Model getModel() throws RepositoryException {
        // we don't need to call super, since the root container does only exists virtually
        Model m = new LinkedHashModel();
        m.add(this.getResourceIRI(), RDF.TYPE, LDP.Container);
        m.add(this.getResourceIRI(), RDFS.LABEL, vf.createLiteral("Platform Root Container"));
        // we get the membership relations from the repository i.e. they are not
        // stored in the root container context but being part of the individual containers
        m.addAll(getReadConnection().getOutgoingStatements(this.getResourceIRI()));
        return m;
    }
    
    @Override
    public void initialize() throws RepositoryException {
        if (!getReadConnection().hasOutgoingStatements(this.getResourceIRI())) {
            LinkedHashModel m = new LinkedHashModel();
            m.add(vf.createStatement(this.getResourceIRI(), RDF.TYPE, LDP.Container));
            m.add(vf.createStatement(this.getResourceIRI(), RDF.TYPE, LDP.Resource));
            m.add(vf.createStatement(this.getResourceIRI(), RDFS.LABEL,
                    vf.createLiteral("Root Container")));
            try (RepositoryConnection connection = getRepository().getConnection()) {
                connection.add(m, getContextIRI());
            }
        }
    }

    @Override
    public IRI add(PointedGraph pointedGraph) throws RepositoryException {
//        if(!SecurityUtils.getSubject().isPermitted(CONTAINER.CREATE_CONTAINER_IN_ROOT))
//            throw new SecurityException("User is not permitted to create new container in platform root container.");
        
        if(!isLDPContainer(pointedGraph))
            throw new IllegalArgumentException("Only LDP Container can be added to the Platform Root Container");
        return super.add(pointedGraph);
    }
    
    private boolean isLDPContainer(PointedGraph pg){
        return !Collections.disjoint(pg.getTypes(), Sets.newHashSet(LDP.Container, LDP.BasicContainer, LDP.DirectContainer));
    }
    
    @Override
    public void delete() throws RepositoryException {
        throw new IllegalArgumentException("Platform Root Container can not be deleted.");
    }
    
}