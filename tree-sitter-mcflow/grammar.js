/**
 * @file Mcflow grammar for tree-sitter
 * @author takenoko4096
 * @license MIT
 */

/// <reference types="tree-sitter-cli/dsl" />
// @ts-check

export default grammar({
  name: "mcflow",

  rules: {
    // TODO: add the actual grammar rules
    source_file: $ => "hello"
  }
});
