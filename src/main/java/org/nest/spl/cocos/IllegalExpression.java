/*
 * Copyright (c)  RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.spl.cocos;

import com.google.common.base.Preconditions;
import de.monticore.cocos.CoCoLog;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.Names;
import org.nest.spl._ast.*;
import org.nest.spl._cocos.*;
import org.nest.symboltable.predefined.PredefinedTypesFactory;
import org.nest.symboltable.symbols.NESTMLTypeSymbol;
import org.nest.symboltable.symbols.NESTMLVariableSymbol;
import org.nest.spl.symboltable.typechecking.ExpressionTypeCalculator;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Check that the type of the loop variable is an integer.
 *
 * @author (last commit) $$Author$$
 * @version $$Revision$$, $$Date$$
 * @since 0.0.1
 */
public class IllegalExpression implements
    SPLASTIF_ClauseCoCo,
    SPLASTFOR_StmtCoCo,
    SPLASTWHILE_StmtCoCo,
    SPLASTAssignmentCoCo,
    SPLASTDeclarationCoCo,
    SPLASTELIF_ClauseCoCo

{
  public static final String ERROR_CODE = "SPL_ILLEGAL_EXPRESSION";

  private final ExpressionTypeCalculator typeCalculator;

  private final PredefinedTypesFactory predefinedTypesFactory;

  public IllegalExpression(final PredefinedTypesFactory predefinedTypesFactory) {
    typeCalculator = new ExpressionTypeCalculator(predefinedTypesFactory);
    this.predefinedTypesFactory = predefinedTypesFactory;
  }


  @Override
  public void check(final ASTAssignment node) {
    // TODO
  }

  @Override
  public void check(final ASTDeclaration node) {
    checkArgument(node.getEnclosingScope().isPresent(), "No scope assigned. Please, run symboltable creator.");
    final Scope scope = node.getEnclosingScope().get();

    // compute the symbol of the var from the declaration.
    // take an arbitrary var since the variables in the declaration
    // share the same type

    if (node.getExpr().isPresent()) {
      final String varNameFromDeclaration = node.getVars().get(0);
      final String declarationTypeName = getDeclarationTypeName(node);
      final Optional<Symbol> varType = scope.resolve(varNameFromDeclaration,
          NESTMLVariableSymbol.KIND);
      Preconditions.checkState(varType.isPresent(), "Cannot resolve the type of the variable:  " + varNameFromDeclaration);

      NESTMLTypeSymbol initializerExpressionType;

      NESTMLTypeSymbol variableDeclarationType;
      try {
        initializerExpressionType = typeCalculator.computeType(node.getExpr().get());
        variableDeclarationType = predefinedTypesFactory.getType(declarationTypeName);
        // TODO write a helper get assignable
        if (!isCompatible(variableDeclarationType, initializerExpressionType)) {
          final String msg = "Cannot initialize variable with an expression of type: " +
              varNameFromDeclaration + " with the type " + initializerExpressionType +
              node.get_SourcePositionStart();
          CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
        }
      }
      catch (RuntimeException e) {
        final String msg = "Cannot determine the type of the initializer expression at " +
            node.get_SourcePositionStart() + " Reason: " + e.getMessage();
        CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
      }

    }

  }



  private String getDeclarationTypeName(final ASTDeclaration declaration) {
    if (declaration.getPrimitiveType().isPresent()) {
      return "boolean";
    }
    if (declaration.getType().isPresent()) {
      return Names.getQualifiedName(declaration.getType().get().getParts());
    }
    throw new RuntimeException("Declaration has not type! Impossible through the grammar.");
  }

  @Override
  public void check(final ASTELIF_Clause node) {
    try {
      if (!typeCalculator.computeType(node.getExpr()).equals(predefinedTypesFactory.getBooleanType())) {
        final String msg = "Cannot use non boolean expression in an if statement " +
            "@" + node.get_SourcePositionStart();
        CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
      }
    }
    catch (RuntimeException e) {
      final String msg = "Cannot initialize variable with an expression of type: " +
          "@" + node.get_SourcePositionStart();
      CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
    }
  }

  @Override
  public void check(final ASTFOR_Stmt node) {
    // TODO
  }

  @Override
  public void check(final ASTIF_Clause node) {
    try {
      if (!typeCalculator.computeType(node.getExpr()).equals(predefinedTypesFactory.getBooleanType())) {
        final String msg = "Cannot use non boolean expression in an if statement " +
            "@" + node.get_SourcePositionStart();
        CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
      }
    }
    catch (RuntimeException e) {
      final String msg = "Cannot use the expression in the if clause. " + e.getMessage() +
          "@" + node.get_SourcePositionStart();
      CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
    }

  }

  @Override
  public void check(final ASTWHILE_Stmt node) {
    try {
      if (!typeCalculator.computeType(node.getExpr()).equals(predefinedTypesFactory.getBooleanType())) {
        final String msg = "Cannot use non boolean expression in a while statement " +
            "@" + node.get_SourcePositionStart();
        CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
      }
    }
    catch (RuntimeException e) {
      final String msg = "Cannot initialize variable with an expression of type: " +
          "@" + node.get_SourcePositionStart();
      CoCoLog.error(ERROR_CODE, msg, node.get_SourcePositionStart());
    }


  }

  private boolean isCompatible(final NESTMLTypeSymbol lhsType, final NESTMLTypeSymbol rhsType) {
    if (lhsType.equals(rhsType)) {
      return true;
    }
    else if (lhsType.equals(predefinedTypesFactory.getRealType()) &&
        rhsType.equals(predefinedTypesFactory.getIntegerType())) {
      return true;
    }
    else if (lhsType.equals(predefinedTypesFactory.getIntegerType()) && rhsType.getType().equals(NESTMLTypeSymbol.Type.UNIT) ||
        rhsType.equals(predefinedTypesFactory.getIntegerType()) && lhsType.getType().equals(NESTMLTypeSymbol.Type.UNIT)) {
      return true;
    }
    else if (lhsType.equals(predefinedTypesFactory.getRealType()) && rhsType.getType().equals(NESTMLTypeSymbol.Type.UNIT) ||
        rhsType.equals(predefinedTypesFactory.getRealType()) && lhsType.getType().equals(NESTMLTypeSymbol.Type.UNIT)) {
      return true;
    }

    return false;
  }

}