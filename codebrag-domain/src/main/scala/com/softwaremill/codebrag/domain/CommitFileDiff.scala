package com.softwaremill.codebrag.domain

case class DiffLine(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String)

case class CommitFileDiff(filename: String, status: String, lines: List[DiffLine])

