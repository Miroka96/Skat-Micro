package user

class User {
    var data: UserData

    constructor() {
        data = UserData()
    }

    constructor(data: UserData) {
        this.data = data
    }


}