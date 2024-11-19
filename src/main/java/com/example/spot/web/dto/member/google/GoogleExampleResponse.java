package com.example.spot.web.dto.member.google;

public class GoogleExampleResponse {

    public static final String EXAMPLE_RESPONSE = """
            {
              "isSuccess": true,
              "code": "COMMON200",
              "message": "OK",
              "result": {
                "tokens": {
                  "accessToken": "eyABCDEFG.eyJtZW1i12341234123xNzMxNzU4MTAxLCJleHAiOjE3MzE3NjE3MDF9.ZplY8yGgO24234FQj0hPB6uY",
                  "refreshToken": "eyABCDEFG.eyJtZW123412341234312hdCI6MTczMTc1ODEwMSwiZXhwIjoxNzMxODQ0NTAxfQ.FGvZ5nL2342342yJ0I7LX-rac",
                  "accessTokenExpiresIn": 3600000
                },
                "email": "example@gmail.com",
                "memberId": 1
              }
            }
            """;
}
