# CityGuideApp
Aplikacja służąca jako przewodnik po Rzeszowie. Aktualnie w wersji beta.

Funkcjonalności:<br>
-pokazywanie aktualnej pozycji użytkownika<br>
-rysowanie trasy którą przebył<br>
-możliwość wybory jednej z dostępnych tras po mieście<br>
-wyświetlanie średniego czasu w korku na danej trasie (dane z google API)<br>
-wyświetlanie odległości do trasy jeżeli na niej nie jesteśmy<br>
-wyświetlanie znacznika na mapie gdy znajdujemy się blisko jednego z ciekawszych miejsc dla danej trasy (geofencing, potrzebna jest lokalizacja z siecii komórkowej)

Aby aplikacja działała poprawnie należy wkleić swój Google Api Key w pliku app/res/values/strings: 

```
<string name="my_google_api_key">Paste your Google APi key Here</string>
```
A następnie:
Plik google-services.json powinien zostać umieszczony w folderze /app 
