{
    "testWebScript": {
        "GET": {
            "blacklist": {
                "user": [
                    "guest",
                    "mario.rossi"
                ],
                "group": [
                    "itc"
                ]
            },
            "whitelist": {
                "user": [
                    "abc"
                ]
            }
        },
        "POST": {
            "whitelist": {
                "user": [
                    "admin"
                ],
                "group": [
                    "si"
                ]
            },
            "blacklist": {
                "all": true
            }
        }
    },
    "i18n": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "common": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "document": {
        "POST": {
            "whitelist": {
                "all": true
            }
        },
        "DELETE": {
            "whitelist": {
                  "user": [
                    "admin"
                ]
            }
        },
        "GET": {
             "user": [
                    "admin"
                ]
        }
    },
    "company/header": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "company/footer": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "loginPage": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "home": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "home/main": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "search/folder/root": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "search/query": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "search/folder/children": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "workflowHistory": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "workflow/history": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "jsConsole": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "jsConsole/main": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "workflowAssociation": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "workflow/association": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "accounting": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "accounting/main": {
        "GET": {
            "whitelist": {
                "user": [
                    "admin"
                ]
            }
        }
    },
    "jbpm$wfcnr:review": {
        "GET": {
            "whitelist": {
                "all": true
            }

        }
    },
    "jbpm$wfcnr:adhoc": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "workflow": {
        "GET": {
            "whitelist": {
                "all": true
            }

        }
    },
    "workflow/main": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "folder": {
        "POST": {
            "whitelist": {
                "all": true
            }
        }
    },
    "node": {
        "POST": {
            "whitelist": {
                "all": true
            }
        },
        "DELETE": {
            "whitelist": {
                "all": true
            }
        }
    },
    "view/": {
        "GET": {
            "whitelist": {
                "all": true
            }
        }
    },
    "crud/cmis/object": {
        "POST": {
            "whitelist": {
                "all": true
            }
        }
    },
    "acl": {
        "GET": {
            "whitelist": {
                "all": true
            }
        },
        "POST": {
            "whitelist": {
                "all": true
            }
        },
        "DELETE": {
            "whitelist": {
                "all": true
            }
        }
    }

}