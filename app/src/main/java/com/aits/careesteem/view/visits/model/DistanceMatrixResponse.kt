package com.aits.careesteem.view.visits.model

data class DistanceMatrixResponse(
    val status: String,
    val rows: List<Row>
)

data class Row(
    val elements: List<Element>
)

data class Element(
    val duration: Duration?,
    val status: String,
)

//data class Duration(
//    val text: String
//)
