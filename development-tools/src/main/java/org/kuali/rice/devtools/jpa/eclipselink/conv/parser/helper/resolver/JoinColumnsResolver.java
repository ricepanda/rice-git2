/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.devtools.jpa.eclipselink.conv.parser.helper.resolver;

import japa.parser.ast.BlockComment;
import japa.parser.ast.Comment;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.kuali.rice.devtools.jpa.eclipselink.conv.parser.helper.NodeData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JoinColumnsResolver extends AbstractJoinColumnResolver {
    private static final Log LOG = LogFactory.getLog(JoinColumnsResolver.class);

    public static final String PACKAGE = "javax.persistence";
    public static final String SIMPLE_NAME = "JoinColumns";

    public JoinColumnsResolver(Collection<DescriptorRepository> descriptorRepositories) {
        super(descriptorRepositories);
    }
    @Override
    public String getFullyQualifiedName() {
        return PACKAGE + "." + SIMPLE_NAME;
    }

    @Override
    protected NodeData getAnnotationNodes(String enclosingClass, String fieldName, String mappedClass) {
        final List<Expression> joinColumns = getJoinColumns(enclosingClass, fieldName, mappedClass);
        if (joinColumns != null && joinColumns.size() > 1) {
            final Comment fixme = new BlockComment("\nFIXME: JPA_CONVERSION\n"
                    + "For compound primary keys, make sure the join columns are in the correct order.\n");
            AnnotationExpr
                    annotation = new SingleMemberAnnotationExpr(new NameExpr(SIMPLE_NAME), new ArrayInitializerExpr(joinColumns));
            annotation.setComment(fixme);
            return new NodeData(annotation,
                    new ImportDeclaration(new QualifiedNameExpr(new NameExpr(PACKAGE), SIMPLE_NAME), false, false),
                    Arrays.asList(
                              new ImportDeclaration(new QualifiedNameExpr(new NameExpr(PrimaryKeyJoinColumnResolver.PACKAGE),PrimaryKeyJoinColumnResolver.SIMPLE_NAME), false, false)
                            , new ImportDeclaration(new QualifiedNameExpr(new NameExpr(JoinColumnResolver.PACKAGE),JoinColumnResolver.SIMPLE_NAME), false, false)
                            ));
        }

        return null;
    }
}
