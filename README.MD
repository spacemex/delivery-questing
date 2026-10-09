# Parcel Questing

> [!WARNING]
> **Please read before proceeding!**
>
> This mod is currently a **work in progress**.
>
> All assets used right now are for development purposes only and will be removed or replaced before release.
>
> Current temporary assets belong to [Henkelmax](https://github.com/henkelmax/delivery-questing).

---

## Todo

- Replace models and textures with new ones
- Add external API capabilities
- Improve documentation
- Refactor to a unique name
- Add better quest, task, shop, and challenge creation
- Create a group management system with permissions

---

# Commands

> [!NOTE]
> All commands begin with:
>
> ```text
> /delivery <group | debug | task>
> ```

## Group Commands

| Command                             | Description                                         |
|-------------------------------------|-----------------------------------------------------|
| `/delivery group accept <group>`    | Accepts an invitation to a group.                   |
| `/delivery group create <name>`     | Creates a group.                                    |
| `/delivery group decline <group>`   | Declines an invitation to a group.                  |
| `/delivery group delete confirm`    | Deletes the group you are the leader of.            |
| `/delivery group info`              | Shows debug information about the group you are in. |
| `/delivery group invite <player>`   | Invites a player to your group.                     |
| `/delivery group leave`             | Leaves your current group.                          |
| `/delivery group remove <player>`   | Removes a player from your group.                   |
| `/delivery group transfer <player>` | Transfers group ownership to another player.        |

## Task Commands

| Command                          | Description                               |
|----------------------------------|-------------------------------------------|
| `/delivery task available`       | Shows all available tasks for your group. |
| `/delivery task accept <taskId>` | Accepts a task for your group.            |
| `/delivery task info <taskId>`   | Shows information about a task.           |
| `/delivery task submit <taskId>` | Submits a completed task.                 |

## Debug Commands

> [!CAUTION]
> Debug commands are mainly for testing and may change often.

| Command                                    | Description                                   |
|--------------------------------------------|-----------------------------------------------|
| `/delivery debug acceptJob <jobId>`        | Forces your group to accept a job.            |
| `/delivery debug addmoney <amount>`        | Adds money to your group.                     |
| `/delivery debug addxp <amount>`           | Adds XP to your group.                        |
| `/delivery debug generatejobmail`          | Generates job mail for your group.            |
| `/delivery debug generatemail`             | Generates mail for your group.                |
| `/delivery debug generatephysicalcontract` | Generates a physical contract for your group. |

---

# Current Data Configuration

> [!NOTE]
> For now, data for **Tasks**, **Jobs**, and **Shop Offers** is stored in:
>
> ```text
> config/DeliveryQuesting
> ```

## Folder Layout

```text
config/DeliveryQuesting
├── config.yml
├── offers.json
├── jobs
│   └── cobblestone_shipment.json
├── tasks
│   └── getting_started
│       └── log_delivery.json
└── skins
```

## Data File Rules

| Type  | Folder / File                         | Repeatable? | Notes                                                                                                               |
|-------|---------------------------------------|------------:|---------------------------------------------------------------------------------------------------------------------|
| Task  | `config/DeliveryQuesting/tasks`       |          No | Each task must be in its own `.json` file. Files can be inside subfolders.                                          |
| Job   | `config/DeliveryQuesting/jobs`        |         Yes | Each job must be in its own `.json` file. Files can be inside subfolders.                                           |
| Offer | `config/DeliveryQuesting/offers.json` |         N/A | Offers are loaded from `offers.json`. The file can contain either a root array or an object with an `offers` array. |

> [!IMPORTANT]
> Tasks and Jobs must use unique IDs.
>
> Duplicate IDs can cause loading errors.

---

# Beginner JSON Guide

## What is JSON?

JSON is a simple text format used to store data.

A JSON file is made from **keys** and **values**.

Example:

```json
{
  "name": "A Stack of Timber",
  "min_level": 0,
  "forced": false
}
```

In this example:

- `"name"` is a key.
- `"A Stack of Timber"` is a string value.
- `"min_level"` is a number value.
- `"forced"` is a boolean value.

## Basic JSON Terms

| Term          | Meaning                                     | Example                                            |
|---------------|---------------------------------------------|----------------------------------------------------|
| String        | Text wrapped in quotes                      | `"A Stack of Timber"`                              |
| Number        | A numeric value                             | `16`, `25`, `100`                                  |
| Boolean       | Either `true` or `false`                    | `false`                                            |
| Object        | A group of keys and values wrapped in `{ }` | `"contractor": { "name": "William" }`              |
| Array         | A list of values wrapped in `[ ]`           | `"dependencies": []`                               |
| Namespaced ID | A unique ID written as `namespace:path`     | `"delivery_questing:getting_started/log_delivery"` |

## JSON Syntax Rules

> [!IMPORTANT]
> JSON is strict. Small formatting mistakes can stop the file from loading.

### Strings need quotes

Correct:

```json
"name": "A Stack of Timber"
```

Incorrect:

```json
"name": A Stack of Timber
```

### Keys need quotes

Correct:

```json
"min_level": 0
```

Incorrect:

```json
min_level: 0
```

### Separate entries with commas

Correct:

```json
{
  "name": "A Stack of Timber",
  "min_level": 0
}
```

Incorrect:

```json
{
  "name": "A Stack of Timber"
  "min_level": 0
}
```

### Do not put a comma after the last entry

Correct:

```json
{
  "name": "A Stack of Timber",
  "min_level": 0
}
```

Incorrect:

```json
{
  "name": "A Stack of Timber",
  "min_level": 0
}
```

---

# What is a Namespaced ID?

A namespaced ID helps prevent conflicts with Minecraft, other mods, or other task packs.

A namespaced ID looks like this:

```text
namespace:path
```

Example:

```text
delivery_questing:getting_started/log_delivery
```

In this example:

| Part                           | Meaning                                                       |
|--------------------------------|---------------------------------------------------------------|
| `delivery_questing`            | The namespace. Usually your mod ID, pack ID, or project name. |
| `getting_started/log_delivery` | The path/name of the entry.                                   |

More examples:

```text
minecraft:bread
minecraft:logs
delivery_questing:cobblestone_shipment
delivery_questing:getting_started/log_delivery
```

---

# Tasks

Tasks are **non-repeatable**. Once completed, they are meant to be finished permanently.

Task files go inside:

```text
config/DeliveryQuesting/tasks
```

Tasks can be placed in subfolders:

```text
config/DeliveryQuesting/tasks/getting_started/log_delivery.json
```

## Example Task

```json
{
  "id": "delivery_questing:getting_started/log_delivery",
  "name": "A Stack of Timber",
  "description": "The local carpenter needs a fresh shipment of logs.",
  "contractor": {
    "name": "William",
    "profession": "Carpenter",
    "skin": "william"
  },
  "min_level": 0,
  "forced": false,
  "dependencies": [],
  "requirements": [
    {
      "type": "item",
      "tag": "minecraft:logs",
      "amount": 16
    }
  ],
  "rewards": {
    "experience": 25,
    "money": 100,
    "items": [
      {
        "item": "minecraft:bread",
        "count": 4
      }
    ]
  }
}
```

## Task Key Reference

| Key            | Type          | Required | Description                                                                                        |
|----------------|---------------|---------:|----------------------------------------------------------------------------------------------------|
| `id`           | Namespaced ID |      Yes | The unique ID for this task. Example: `delivery_questing:getting_started/log_delivery`.            |
| `name`         | String        |      Yes | The display name shown to the player.                                                              |
| `description`  | String        |      Yes | A short explanation of the task.                                                                   |
| `contractor`   | Object        |      Yes | Information about who is giving the task.                                                          |
| `min_level`    | Number        |      Yes | Minimum group/player level required before this task can appear. Use `0` for no level requirement. |
| `forced`       | Boolean       |      Yes | Whether this task is forced or prioritized. Usually `false`.                                       |
| `dependencies` | Array         |      Yes | A list of task IDs that must be completed before this task is available. Use `[]` if none.         |
| `requirements` | Array         |      Yes | A list of requirements the player must complete.                                                   |
| `rewards`      | Object        |      Yes | Rewards given after completing the task.                                                           |

## Task Dependencies

Dependencies let you make one task require another task first.

```json
"dependencies": [
  "delivery_questing:getting_started/first_delivery",
  "delivery_questing:getting_started/meet_the_carpenter"
]
```

If there are no dependencies, use an empty array:

```json
"dependencies": []
```

> [!CAUTION]
> Do not create dependency loops.
>
> Example of a bad loop:
>
> - Task A requires Task B
> - Task B requires Task A

---

# Jobs

Jobs are **repeatable**. They are intended to be completed more than once.

Job files go inside:

```text
config/DeliveryQuesting/jobs
```

Jobs can also be placed in subfolders:

```text
config/DeliveryQuesting/jobs/mining/cobblestone_shipment.json
```

## Example Job

```json
{
  "id": "delivery_questing:cobblestone_shipment",
  "name": "Cobblestone Shipment",
  "description": "A repeatable order for bulk cobblestone.",
  "contractor": {
    "name": "Stoneworks Inc.",
    "profession": "Construction Supplier",
    "skin": "william"
  },
  "min_level": 10,
  "requirements": [
    {
      "type": "item",
      "item": "minecraft:cobblestone",
      "amount": 16
    }
  ],
  "rewards": {
    "experience": 5,
    "money": 40,
    "items": []
  }
}
```

## Job Key Reference

| Key            | Type          | Required | Description                                                                    |
|----------------|---------------|---------:|--------------------------------------------------------------------------------|
| `id`           | Namespaced ID |      Yes | The unique ID for this job. Example: `delivery_questing:cobblestone_shipment`. |
| `name`         | String        |      Yes | The display name shown to the player.                                          |
| `description`  | String        |      Yes | A short explanation of the job.                                                |
| `contractor`   | Object        |      Yes | Information about who is giving the job.                                       |
| `min_level`    | Number        |      Yes | Minimum group/player level required before this job can appear.                |
| `requirements` | Array         |      Yes | A list of requirements the player must complete.                               |
| `rewards`      | Object        |      Yes | Rewards given after completing the job.                                        |

> [!NOTE]
> Jobs do not use `forced` or `dependencies`.
>
> Those are task-only fields.

---

# Contractor Object

The `contractor` object describes who is giving the task or job.

```json
"contractor": {
  "name": "William",
  "profession": "Carpenter",
  "skin": "william"
}
```

| Key          | Type   | Required | Description                            |
|--------------|--------|---------:|----------------------------------------|
| `name`       | String |      Yes | The contractor's display name.         |
| `profession` | String |      Yes | The contractor's job or role.          |
| `skin`       | String |      Yes | The skin name used for the contractor. |

> [!NOTE]
> If you do not have a custom skin ready yet, you may use an empty string:
>
> ```json
> "skin": ""
> ```

---

# Requirements

The `requirements` array contains one or more things the player must provide or complete.

Currently, item requirements look like this:

```json
"requirements": [
  {
    "type": "item",
    "item": "minecraft:cobblestone",
    "amount": 16
  }
]
```

Or with an item tag:

```json
"requirements": [
  {
    "type": "item",
    "tag": "minecraft:logs",
    "amount": 16
  }
]
```

## Item Requirement Keys

| Key      | Type          |  Required | Description                                                |
|----------|---------------|----------:|------------------------------------------------------------|
| `type`   | String        |       Yes | The requirement type. For item requirements, use `"item"`. |
| `item`   | Namespaced ID | Sometimes | A specific item ID. Example: `minecraft:cobblestone`.      |
| `tag`    | Namespaced ID | Sometimes | An item tag. Example: `minecraft:logs`.                    |
| `amount` | Number        |       Yes | How many items are required.                               |

> [!IMPORTANT]
> Use either `item` or `tag`.
>
> - Use `item` when you want one exact item.
> - Use `tag` when you want to allow any item from a tag.
>
> Example:
>
> ```json
> "tag": "minecraft:logs"
> ```
>
> This allows any item in the `minecraft:logs` tag.

---

# Rewards

The `rewards` object controls what the player receives after completing a task or job.

```json
"rewards": {
  "experience": 25,
  "money": 100,
  "items": [
    {
      "item": "minecraft:bread",
      "count": 4
    }
  ]
}
```

## Rewards Key Reference

| Key          | Type   | Required | Description                                           |
|--------------|--------|---------:|-------------------------------------------------------|
| `experience` | Number |      Yes | Amount of experience rewarded.                        |
| `money`      | Number |      Yes | Amount of money rewarded.                             |
| `items`      | Array  |      Yes | A list of item rewards. Use `[]` for no item rewards. |

## Item Reward Keys

Each object inside `rewards.items` gives the player an item.

```json
{
  "item": "minecraft:bread",
  "count": 4
}
```

| Key     | Type          | Required | Description                    |
|---------|---------------|---------:|--------------------------------|
| `item`  | Namespaced ID |      Yes | The item ID to reward.         |
| `count` | Number        |      Yes | How many of that item to give. |

If there are no item rewards, use:

```json
"items": []
```

---

# Shop Offers

Offers are shop entries that players can buy.

Offers are loaded from:

```text
config/DeliveryQuesting/offers.json
```

## Example Offers File

```json
{
  "offers": [
    {
      "id": "delivery_questing:bread_pack",
      "item": "minecraft:bread",
      "count": 8,
      "price": 50,
      "min_level": 0,
      "for_every_member": false
    },
    {
      "id": "delivery_questing:oak_logs",
      "item": "minecraft:oak_log",
      "count": 16,
      "price": 120,
      "min_level": 0,
      "for_every_member": false
    },
    {
      "id": "delivery_questing:iron_ingots",
      "item": "minecraft:iron_ingot",
      "count": 8,
      "price": 350,
      "min_level": 3,
      "for_every_member": false
    },
    {
      "id": "delivery_questing:starter_food_each_member",
      "item": "minecraft:cooked_beef",
      "count": 4,
      "price": 100,
      "min_level": 1000,
      "for_every_member": true
    },
    {
      "id": "delivery_questing:diamond",
      "item": "minecraft:diamond",
      "count": 1,
      "price": 1500,
      "min_level": 10,
      "for_every_member": false
    }
  ]
}
```

## Offer Key Reference

| Key                | Type          | Required | Default | Description                                                       |
|--------------------|---------------|---------:|--------:|-------------------------------------------------------------------|
| `id`               | Namespaced ID |      Yes |    None | Unique ID for the offer.                                          |
| `item`             | Namespaced ID |      Yes |    None | Item being sold.                                                  |
| `count`            | Number        |       No |     `1` | How many items are given when bought. Must be greater than `0`.   |
| `price`            | Number        |      Yes |    None | Cost of the offer. Must be greater than `0`.                      |
| `min_level`        | Number        |       No |     `0` | Minimum level required to buy this offer.                         |
| `for_every_member` | Boolean       |       No | `false` | If `true`, the offer gives the item/count for every group member. |

## Alternative Offers Format

The offers file may also be written as a root array:

```json
[
  {
    "id": "delivery_questing:bread_pack",
    "item": "minecraft:bread",
    "count": 8,
    "price": 50,
    "min_level": 0,
    "for_every_member": false
  }
]
```

Both formats are valid:

```json
{
  "offers": []
}
```

```json
[]
```

---

# Minimal Examples

## Minimal Task

```json
{
  "id": "delivery_questing:getting_started/example_task",
  "name": "Example Task",
  "description": "This is an example non-repeatable task.",
  "contractor": {
    "name": "William",
    "profession": "Carpenter",
    "skin": ""
  },
  "min_level": 0,
  "forced": false,
  "dependencies": [],
  "requirements": [],
  "rewards": {
    "experience": 0,
    "money": 0,
    "items": []
  }
}
```

## Minimal Job

```json
{
  "id": "delivery_questing:example_job",
  "name": "Example Job",
  "description": "This is an example repeatable job.",
  "contractor": {
    "name": "William",
    "profession": "Worker",
    "skin": ""
  },
  "min_level": 0,
  "requirements": [],
  "rewards": {
    "experience": 0,
    "money": 0,
    "items": []
  }
}
```

## Minimal Offers File

```json
{
  "offers": [
    {
      "id": "delivery_questing:example_offer",
      "item": "minecraft:bread",
      "count": 1,
      "price": 10,
      "min_level": 0,
      "for_every_member": false
    }
  ]
}
```

---

# Common Mistakes

## Missing comma

Incorrect:

```json
{
  "name": "Example"
  "min_level": 0
}
```

Correct:

```json
{
  "name": "Example",
  "min_level": 0
}
```

## Trailing comma

Incorrect:

```json
{
  "name": "Example",
  "min_level": 0,
}
```

Correct:

```json
{
  "name": "Example",
  "min_level": 0
}
```

## Invalid namespaced ID

Incorrect:

```json
"id": "log_delivery"
```

Correct:

```json
"id": "delivery_questing:log_delivery"
```

## Using both `item` and `tag`

Avoid this:

```json
{
  "type": "item",
  "item": "minecraft:oak_log",
  "tag": "minecraft:logs",
  "amount": 16
}
```

Use one or the other:

```json
{
  "type": "item",
  "tag": "minecraft:logs",
  "amount": 16
}
```

---

# Quick Checklist

Before starting the game, check that:

- Your JSON file ends with `.json`.
- Strings are wrapped in quotes.
- Every key is wrapped in quotes.
- There are no trailing commas.
- IDs use the `namespace:path` format.
- Task IDs are unique.
- Job IDs are unique.
- Offer IDs are unique.
- Tasks are inside `config/DeliveryQuesting/tasks`.
- Jobs are inside `config/DeliveryQuesting/jobs`.
- Offers are inside `config/DeliveryQuesting/offers.json`.

> [!TIP]
> If something fails to load, check the latest log file. JSON errors usually tell you which file has the issue.