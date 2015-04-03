(function () {
	var app = angular.module('myse', []);

	app.controller('NavigationController', function () {
		this.current = 'search';
		this.select = function (page) {
			this.current = page;
		};
		this.isSelected = function (page) {
			//console.log(this.current + ' / ' + page);
			return this.current === page;
		};
		this.class = function (page) {
			return {
				active: this.isSelected(page)
			};
		};
	});

	app.directive('searchList', function () {
		return {
			restrict: 'E',
			templateUrl: '/static/search.html',
			controller: ['$http', function ($http) {
					// CONTROLLER CODE
					this.query = '';
					this.response = {
						results: [],
						error: null
					};
					this.queryChanged = function () {
						var ctrl = this;
						$http.post('/rest/search', {'q': this.query}).success(
								function (data) {
									ctrl.response = data;
								}
						);
						$log.log(this.response);
					};
				}
			],
			controllerAs: 'search'
		};
	});

})();

$('#core').show();
