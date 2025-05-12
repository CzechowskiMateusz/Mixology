package pl.domain.application.mixology

data class Cocktail (
    var ID_Drink: Int = 0,
    var Name: String = "",
    var Author: String = "",
    var Description: String = "",
    var List_of_mixture: List<String> = emptyList(),
    var Step_Guide: List<String> = emptyList(),
    var ImageUrl: String = "",
    var Alcoholic: Int = 0
)