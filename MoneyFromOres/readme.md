
<p align="center">
  <img src="https://i.imgur.com/q2lWFrC.jpeg" alt="MoneyFromOres Banner">
</p>

# 💰 MoneyFromOres

Un plugin Minecraft (1.21+) qui récompense les joueurs avec de l'argent lorsqu'ils minent des minerais.  
Compatible avec **Vault** et tout plugin d'économie !  
Développé par **QuantumCraft-Studio** 👑

---

## ✨ Fonctionnalités

- 💵 Gagne de l'argent en minant des minerais (via Vault)
- 🎲 Système de **chance aléatoire** pour rendre les récompenses plus rares
- 📈 Statistiques sauvegardées par joueur
- 🔧 Configuration complète : récompenses, sons, particules...
- ⛏️ Anti-farm : aucun gain sur les blocs placés par le joueur
- 🔊 Effets visuels/sonores personnalisables
- 🧙 Multiplicateurs de gains via permissions
- ✅ Messages activables/désactivables avec `/mfo toggle`

---

## 📦 Installation

1. Télécharge le `.jar` depuis [releases](#)
2. Place-le dans le dossier `/plugins` de ton serveur Paper/Spigot
3. Assure-toi d’avoir **Vault** + un plugin d’économie compatible (ex: EssentialsX)
4. Redémarre le serveur ✅

---

## ⚙️ Configuration

```yaml
chance: 0.3 # 30% de chance d'obtenir une récompense par minerai

rewards:
  DIAMOND_ORE: 20.0
  IRON_ORE: 10.0
  # ... (autres minerais)

messages:
  reward: "§aTu as gagné {amount} money pour avoir miné un {ore} ! (x{multiplier})"
  anti_farm: "§cPas de récompense : ce minerai a été placé manuellement."
  no_reward: "§7Ce minerai ne donne pas de récompense."

effects:
  enabled: true
  sound: ENTITY_EXPERIENCE_ORB_PICKUP
  particle: VILLAGER_HAPPY
```

---

## 🔧 Commandes

| Commande          | Description                              | Permission requise                 |
|-------------------|------------------------------------------|------------------------------------|
| `/mfo toggle`     | Active ou désactive les messages de gain | `moneyfromores.toggle`             |
| `/mfo stats`      | Affiche les stats du joueur              | `moneyfromores.stats`              |
| `/mfo top`        | Classement des meilleurs mineurs         | `moneyfromores.top`                |
| `/mfo reload`     | Recharge la config du plugin             | `moneyfromores.reload`             |

---

## 🧙 Multiplicateurs

Ajoutez des permissions pour augmenter les gains :
- `moneyfromores.multiplier.2` → x2
- `moneyfromores.multiplier.5` → x5
- jusqu'à `moneyfromores.multiplier.10`

---

## 📈 Statistiques

Les stats sont enregistrées dans `stats.yml` :
- Nombre total de minerais minés
- Argent total gagné

---

## 🧪 À venir (TODO)

- GUI pour les statistiques
- Messages d’événements rares
- Intégration PlaceholderAPI
- Support BungeeCord

---

## 👑 Auteur

Développé par **QuantumCraft-Studio**  
🔗 Site : [www.quantumcraft-studios.com](https://quantumcraft-studios.com/)  
📧 Contact : `contact@quantumcraft-studios.com`

---

## ✅ Dépendances

- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [EssentialsX](https://essentialsx.net/) (ou autre plugin économique)

---

## ❤️ Merci

Merci d’utiliser **MoneyFromOres** !  
Un projet fait avec amour, café, et beaucoup de `System.out.println()` ☕️
