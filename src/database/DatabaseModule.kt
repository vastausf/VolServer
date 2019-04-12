@file:Suppress("RemoveExplicitTypeArguments")

import database.UserAlreadyExists

class DatabaseModule(
    private val volGraphDatabase: VolGraphDatabase
) {
    fun createUser(user: UsersCreateI): UserNode {
        val users = volGraphDatabase.findNode<UserNode> {
            login = user.login.trimAllSpaces()
        }

        if (users.isEmpty())
            return volGraphDatabase.newNode<UserNode> {
                login = user.login.trimAllSpaces()
                password = user.password.trimAllSpaces()
                firstName = user.data.firstName.trimAllSpaces()
                lastName = user.data.lastName.trimAllSpaces()
                middleName = user.data.middleName.trimAllSpaces()
                birthday = user.data.birthday
                about = user.data.about?.trimAllSpaces()
                phoneNumber = user.data.phoneNumber?.trimAllSpaces()
                image = user.data.image?.trimAllSpaces() ?: "default"
                email = user.data.email?.trimAllSpaces()
                link = user.data.link?.trimAllSpaces()
            }
        else {
            throw UserAlreadyExists()
        }
    }

    fun findByLoginAndPassword(login: String, password: String): UserNode? {
        return volGraphDatabase.findNode<UserNode> {
            this.login = login
            this.password = password
        }.firstOrNull()
    }

    fun findUserById(id: Long): UserNode? {
        return volGraphDatabase.findNode<UserNode> {
            this.id = id
        }.firstOrNull()
    }

    fun editUser(user: UserDataEdit, userId: Long): UserData? {
        return volGraphDatabase.editNode<UserNode>(userId) {
            user.firstName?.let { firstName = it }
            user.lastName?.let { lastName = it }
            user.middleName?.let { middleName = it }
            user.birthday?.let { birthday = it }

            about = user.about
            phoneNumber = user.phoneNumber
            image = user.image
            email = user.email
            link = user.link
        }.let {
            UserData(
                it.id,
                it.firstName,
                it.lastName,
                it.middleName,
                it.birthday,
                it.about,
                it.phoneNumber,
                it.image?.createImageLink(),
                it.email,
                it.link
            )
        }
    }

    fun findUsersByParameters(parameters: UserDataSearch, offset: Long, amount: Long): List<UserData> {
        return volGraphDatabase.findNode<UserNode>(amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.firstName?.let {
                filter.add { className ->
                    "$className.firstName" like it
                }
            }

            parameters.lastName?.let {
                filter.add { className ->
                    "$className.lastName" like it
                }
            }

            parameters.middleName?.let {
                filter.add { className ->
                    "$className.middleName" like it
                }
            }

            parameters.birthdayMin?.let {
                filter.add { className ->
                    "$className.birthday" greaterOrEquals it
                }
            }

            parameters.birthdayMax?.let {
                filter.add { className ->
                    "$className.birthday" lessOrEquals it
                }
            }

            parameters.about?.let {
                filter.add { className ->
                    "$className.about" like it
                }
            }

            parameters.phoneNumber?.let {
                filter.add { className ->
                    "$className.phoneNumber" like it
                }
            }

            parameters.email?.let {
                filter.add { className ->
                    "$className.email" like it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }
        }.map {
            UserData(
                it.id,
                it.firstName,
                it.lastName,
                it.middleName,
                it.birthday,
                it.about,
                it.phoneNumber,
                it.image?.createImageLink(),
                it.email,
                it.link
            )
        }
    }


    fun createGroup(group: GroupCreateData, userId: Long): GroupFullData {
        val groupNode = volGraphDatabase.newNode<GroupNode> {
            group.description?.let { description = it }
            group.color?.let { color = it }
            group.image?.let { image = it }
            group.link?.let { link = it }

            title = group.title.trimAllSpaces()
            creatorId = userId
        }

        volGraphDatabase.newRelationship<GroupCreatorRelationship>(userId, groupNode.id)

        return groupNode.let {
            GroupFullData(
                it.id,
                it.title,
                it.description,
                it.color,
                it.image?.createImageLink(),
                it.link,
                it.creatorId,
                it.joined > 0,
                it.administrated > 0,
                it.memberCount
            )
        }
    }

    fun editGroup(group: GroupDataEdit, groupId: Long): GroupFullData {
        return volGraphDatabase.editNode<GroupNode>(groupId) {
            group.title?.let { title = it.trimAllSpaces() }
            group.description?.let { description = it.trimAllSpaces() }
            group.color?.let { color = it.trimAllSpaces() }
            group.image?.let { image = it.trimAllSpaces() }
            group.link?.let { link = it.trimAllSpaces() }
        }.let {
            GroupFullData(
                it.id,
                it.title,
                it.description,
                it.color,
                it.image?.createImageLink(),
                it.link,
                it.creatorId,
                it.joined > 0,
                it.administrated > 0,
                it.memberCount
            )
        }
    }

    fun findGroupsByParameters(userId: Long, parameters: GroupDataSearch, offset: Long, amount: Long): List<GroupFullData> {
        return volGraphDatabase.findNodeRelatedWith<UserNode, GroupNode>(userId, amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.title?.let {
                filter.add { className ->
                    "$className.title" like it
                }
            }

            parameters.description?.let {
                filter.add { className ->
                    "$className.description" like it
                }
            }

            parameters.canPost?.let {
                filter.add { className ->
                    "$className.canPost" equals it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }

            parameters.creatorIds?.let {
                filter.add { className ->
                    "$className.creatorId" inList it
                }
            }
        }.map {
            GroupFullData(
                it.id,
                it.title,
                it.description,
                it.color,
                it.image?.createImageLink(),
                it.link,
                it.creatorId,
                it.joined > 0,
                it.administrated > 0,
                it.memberCount
            )
        }
    }

    fun findGroupsByUser(userId: Long, parameters: GroupDataSearch, offset: Long, amount: Long): List<GroupFullData> {
        return volGraphDatabase.findRelationshipNode<GroupNode, GroupJoinedRelationship>(userId, amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.title?.let {
                filter.add { className ->
                    "$className.title" like it
                }
            }

            parameters.description?.let {
                filter.add { className ->
                    "$className.description" like it
                }
            }

            parameters.canPost?.let {
                filter.add { className ->
                    "$className.canPost" equals it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }

            parameters.creatorIds?.let {
                filter.add { className ->
                    "$className.creatorId" inList it
                }
            }
        }.map {
            GroupFullData(
                it.id,
                it.title,
                it.description,
                it.color,
                it.image?.createImageLink(),
                it.link,
                it.creatorId,
                it.joined > 0,
                it.administrated > 0,
                it.memberCount
            )
        }
    }

    fun findUsersByGroup(groupId: Long, parameters: UserDataSearch, offset: Long, amount: Long): List<UserData> {
        return volGraphDatabase.findRelationshipNode<UserNode, GroupJoinedRelationship>(groupId, amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.firstName?.let {
                filter.add { className ->
                    "$className.firstName" like it
                }
            }

            parameters.lastName?.let {
                filter.add { className ->
                    "$className.lastName" like it
                }
            }

            parameters.middleName?.let {
                filter.add { className ->
                    "$className.middleName" like it
                }
            }

            parameters.birthdayMin?.let {
                filter.add { className ->
                    "$className.birthday" greaterOrEquals it
                }
            }

            parameters.birthdayMax?.let {
                filter.add { className ->
                    "$className.birthday" lessOrEquals it
                }
            }

            parameters.about?.let {
                filter.add { className ->
                    "$className.about" like it
                }
            }

            parameters.phoneNumber?.let {
                filter.add { className ->
                    "$className.phoneNumber" like it
                }
            }

            parameters.email?.let {
                filter.add { className ->
                    "$className.email" like it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }
        }.map {
            UserData(
                it.id,
                it.firstName,
                it.lastName,
                it.middleName,
                it.birthday,
                it.about,
                it.phoneNumber,
                it.image?.createImageLink(),
                it.email,
                it.link
            )
        }
    }

    fun joinGroup(administrator: Boolean, groupId: Long, userId: Long): Boolean {
        val createdRelationship = if (administrator)
            volGraphDatabase.newRelationship<GroupAdministratorRelationship>(userId, groupId)
        else {
            volGraphDatabase.newRelationship<GroupJoinedRelationship>(userId, groupId)
        }

        return createdRelationship != null
    }

    fun leaveGroup(groupId: Long, userId: Long): Boolean {
        val deletedRelationships = volGraphDatabase.deleteRelationship<GroupAdministratorRelationship>(userId, groupId) +
                volGraphDatabase.deleteRelationship<GroupJoinedRelationship>(userId, groupId)

        return deletedRelationships > 0
    }

    fun isGroupAdministrator(userId: Long, groupId: Long): Boolean {
        return volGraphDatabase.findNodeRelatedWith<UserNode, GroupNode>(userId) {
            id = groupId
        }.first().administrated > 0
    }

    fun isEventCreator(userId: Long, eventId: Long): Boolean {
        return volGraphDatabase.findNode<EventNode> {
            id = eventId
            creatorId = userId
        }.isNotEmpty()
    }


    fun createEvent(event: EventCreateData, userId: Long): EventFullData {
        val eventNode = volGraphDatabase.newNode<EventNode> {
            creatorId = userId
            title = event.title
            place = event.place
            datetime = event.datetime
            duration = event.duration
            description = event.description
            link = event.link
        }

        volGraphDatabase.newRelationship<EventCreatorRelationship>(userId, eventNode.id)

        return eventNode.let {
            EventFullData(
                it.id,
                it.title,
                it.creatorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link,
                it.image?.createImageLink(),
                it.liked > 0,
                it.likeCount,
                it.joined > 0,
                it.joinCount
            )
        }
    }

    fun editEvent(event: EventsDataEdit, eventId: Long): EventFullData {
        return volGraphDatabase.editNode<EventNode>(eventId) {
            event.title?.let { title = it.trimAllSpaces() }
            event.place?.let { place = it.trimAllSpaces() }
            event.datetime?.let { datetime = it }
            event.duration?.let { duration = it }
            event.image?.let { image = it }
            event.description?.let { description = it.trimAllSpaces() }
            event.link?.let { link = it.trimAllSpaces() }
        }.let {
            EventFullData(
                it.id,
                it.title,
                it.creatorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link,
                it.image?.createImageLink(),
                it.liked > 0,
                it.likeCount,
                it.joined > 0,
                it.joinCount
            )
        }
    }

    fun findEventsByParameters(parameters: EventDataSearch, offset: Long, amount: Long, byUser: Long): List<EventFullData> {
        return volGraphDatabase.findNode<EventNode>(byUser, amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.title?.let {
                filter.add { className ->
                    "$className.title" like it
                }
            }

            parameters.creatorIds?.let {
                filter.add { className ->
                    "$className.creatorId" inList it
                }
            }

            parameters.place?.let {
                filter.add { className ->
                    "$className.place" like it
                }
            }

            parameters.datetimeMin?.let {
                filter.add { className ->
                    "$className.datetime" greaterOrEquals it
                }
            }

            parameters.datetimeMax?.let {
                filter.add { className ->
                    "$className.datetime" lessOrEquals it
                }
            }

            parameters.durationMin?.let {
                filter.add { className ->
                    "$className.duration" greaterOrEquals it
                }
            }

            parameters.durationMax?.let {
                filter.add { className ->
                    "$className.duration" lessOrEquals it
                }
            }

            parameters.description?.let {
                filter.add { className ->
                    "$className.description" like it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }
        }.map {
            EventFullData(
                it.id,
                it.title,
                it.creatorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link,
                it.image?.createImageLink(),
                it.liked > 0,
                it.likeCount,
                it.joined > 0,
                it.joinCount
            )
        }
    }

    fun findEventsByUser(userId: Long, parameters: EventDataSearch, offset: Long, amount: Long): List<EventFullData> {
        return volGraphDatabase.findRelationshipNode<EventNode, EventJoinedRelationship>(userId, amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.title?.let {
                filter.add { className ->
                    "$className.title" like it
                }
            }

            parameters.description?.let {
                filter.add { className ->
                    "$className.description" like it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }

            parameters.creatorIds?.let {
                filter.add { className ->
                    "$className.creatorId" inList it
                }
            }
        }.map {
            EventFullData(
                it.id,
                it.title,
                it.creatorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link,
                it.image?.createImageLink(),
                it.liked > 0,
                it.likeCount,
                it.joined > 0,
                it.joinCount
            )
        }
    }

    fun findUsersByEvent(eventId: Long, parameters: UserDataSearch, offset: Long, amount: Long): List<UserData> {
        return volGraphDatabase.findRelationshipNode<UserNode, EventJoinedRelationship>(eventId, amount, offset) { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }

            parameters.firstName?.let {
                filter.add { className ->
                    "$className.firstName" like it
                }
            }

            parameters.lastName?.let {
                filter.add { className ->
                    "$className.lastName" like it
                }
            }

            parameters.middleName?.let {
                filter.add { className ->
                    "$className.middleName" like it
                }
            }

            parameters.birthdayMin?.let {
                filter.add { className ->
                    "$className.birthday" greaterOrEquals it
                }
            }

            parameters.birthdayMax?.let {
                filter.add { className ->
                    "$className.birthday" lessOrEquals it
                }
            }

            parameters.about?.let {
                filter.add { className ->
                    "$className.about" like it
                }
            }

            parameters.phoneNumber?.let {
                filter.add { className ->
                    "$className.phoneNumber" like it
                }
            }

            parameters.email?.let {
                filter.add { className ->
                    "$className.email" like it
                }
            }

            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }
        }.map {
            UserData(
                it.id,
                it.firstName,
                it.lastName,
                it.middleName,
                it.birthday,
                it.about,
                it.phoneNumber,
                it.image?.createImageLink(),
                it.email,
                it.link
            )
        }
    }

    fun joinEvent(eventId: Long, userId: Long): Boolean {
        val createdRelationship = volGraphDatabase.newRelationship<EventJoinedRelationship>(userId, eventId)

        return createdRelationship != null
    }

    fun leaveEvent(eventId: Long, userId: Long): Boolean {
        val deletedRelationships = volGraphDatabase.deleteRelationship<EventJoinedRelationship>(userId, eventId)

        return deletedRelationships > 0
    }

    fun likeEvent(eventId: Long, userId: Long, state: Boolean): Boolean {
        return if (state) {
            volGraphDatabase.newRelationship<EventLikedRelationship>(userId, eventId) != null
        } else {
            volGraphDatabase.deleteRelationship<EventLikedRelationship>(userId, eventId) > 0
        }
    }

    fun findLikedEvents(userId: Long, offset: Long, amount: Long): List<EventFullData> {
        return volGraphDatabase.findRelationshipNode<EventNode, EventLikedRelationship>(userId, amount, offset){ }.map {
            EventFullData(
                it.id,
                it.title,
                it.creatorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link,
                it.image?.createImageLink(),
                it.liked > 0,
                it.likeCount,
                it.joined > 0,
                it.joinCount
            )
        }
    }
}
