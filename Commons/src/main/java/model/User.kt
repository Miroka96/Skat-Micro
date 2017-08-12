package model

import model.database.UserData

class User {
    var data: UserData

    constructor() {
        data = UserData()
    }

    constructor(data: UserData) {
        this.data = data
    }


}