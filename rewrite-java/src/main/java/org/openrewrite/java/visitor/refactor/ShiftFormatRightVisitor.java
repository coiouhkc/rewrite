/*
 * Copyright 2020 the original authors.
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
package org.openrewrite.java.visitor.refactor;

import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

public class ShiftFormatRightVisitor extends ScopedJavaRefactorVisitor {
    private final String shift;

    public ShiftFormatRightVisitor(UUID scope, int shift, boolean isIndentedWithSpaces) {
        super(scope);
        this.shift = range(0, shift)
                .mapToObj(n -> isIndentedWithSpaces ? " " : "\t")
                .collect(Collectors.joining(""));
    }

    @Override
    public J visitElse(J.If.Else elze) {
        J.If.Else e = refactor(elze, super::visitElse);
        if (isScopeInCursorPath() && isOnOwnLine(elze)) {
            e = e.withPrefix(e.getFormatting().getPrefix() + shift);
        }
        return e;
    }

    @Override
    public J visitStatement(Statement statement) {
        Statement s = refactor(statement, super::visitStatement);
        if (isScopeInCursorPath() && isOnOwnLine(statement)) {
            s = s.withPrefix(s.getFormatting().getPrefix() + shift);
        }
        return s;
    }

    @Override
    public J visitBlock(J.Block<Tree> block) {
        J.Block<Tree> b = refactor(block, super::visitBlock);
        if (isScopeInCursorPath()) {
            b = b.withEndOfBlockSuffix(b.getEndOfBlockSuffix() + shift);
        }
        return b;
    }

    private boolean isOnOwnLine(Tree tree) {
        return tree.getFormatting().getPrefix().chars().takeWhile(c -> c == '\n' || c == '\r').count() > 0;
    }
}
