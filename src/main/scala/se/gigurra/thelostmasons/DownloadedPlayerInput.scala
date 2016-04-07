package se.gigurra.thelostmasons

case class DownloadedPlayerInput(age: Double /* Server age */,
                                 timestamp: Double /* utc time stamp*/,
                                 data: PlayerInput)