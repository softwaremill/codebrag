package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.domain.Like

case class LikeEvent(like: Like) extends Event
