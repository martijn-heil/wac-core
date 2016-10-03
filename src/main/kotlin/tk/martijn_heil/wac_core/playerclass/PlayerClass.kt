package tk.martijn_heil.wac_core.playerclass


class PlayerClass(val id: String,
                  val commandsExecutedOnRespawn: List<String>,
                  val kitName: String
)
{



    companion object {
        val playerClasses: List<PlayerClass> = listOf(
                PlayerClass("test", listOf(), ""),
                PlayerClass("test2", listOf(), "")
        )

        val default = playerClasses[0];

        fun playerClassExists(id: String): Boolean {
            playerClasses.forEach { if(it.id.equals(id)) return true; }
            return false;
        }

        fun fromId(id: String): PlayerClass? {
            return playerClasses.find { it.id.equals(id) }
        }
    }
}