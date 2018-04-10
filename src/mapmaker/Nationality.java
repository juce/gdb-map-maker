package mapmaker;

public class Nationality
{
    public static String[] names = {
        "Austria", "Belgium", "Bulgaria", "Croatia", "Czech Republic", "Denmark", "England", "Finland",
        "France", "Germany", "Greece", "Hungary", "Ireland", "Italy", "Latvia", "Netherlands",
        "Northern Ireland", "Norway", "Poland", "Portugal", "Romania", "Russia", "Scotland",
        "Serbia and Montenegro", "Slovakia", "Slovenia", "Spain", "Sweden", "Switzerland", "Turkey", "Ukraine",
        "Wales", "Cameroon", "Cote d'Ivoire", "Morocco", "Nigeria", "Senegal", "South Africa", "Tunisia",
        "Costa Rica", "Mexico", "USA", "Argentina", "Brazil", "Chile", "Colombia", "Ecuador",
        "Paraguay", "Peru", "Uruguay", "Venezuela", "China", "Iran", "Japan", "Saudi Arabia",
        "South Korea", "Australia", "Albania", "Armenia", "Belarus", "Bosnia and Herzegovina", "Cyprus", "Georgia",
        "Estonia", "Faroe Islands", "Iceland", "Israel", "Lithuania", "Luxembourg", "Macedonia", "Moldova",
        "Algeria", "Angola", "Burkina Faso", "Cape Verde", "Congo", "DR Congo", "Egypt", "Equatorial Guinea",
        "Gabon", "Gambia", "Ghana", "Guinea", "Guinea-Bissau", "Liberia", "Libya", "Mali",
        "Mauritius", "Mozambique", "Namibia", "Sierra Leone", "Togo", "Zambia", "Zimbabwe", "Canada",
        "Grenada", "Guadeloupe", "Guatemala", "Honduras", "Jamaica", "Martinique", "Netherlands Antilles", "Panama",
        "Trinidad and Tobago", "Bolivia", "Guyana", "Uzbekistan", "New Zealand", "Free Nationality"
    };

    int id;
    String name;

    public Nationality(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
